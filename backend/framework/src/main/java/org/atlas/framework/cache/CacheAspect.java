package org.atlas.framework.cache;

import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect to handle caching functionality for methods annotated with @Cache.
 * <p>
 * This aspect intercepts method calls and implements caching logic: 1. Check if cached value exists
 * for the given key 2. If exists, return cached value 3. If not exists, execute method and cache
 * the result
 */
@Aspect
@Component
public class CacheAspect {

  private final CachePort cachePort;

  public CacheAspect(CachePort cachePort) {
    this.cachePort = cachePort;
  }

  /**
   * Around advice for methods annotated with @Cache. Implements cache-aside pattern.
   */
  @Around("@annotation(cache)")
  public Object handleCaching(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
    // Use cache key directly from annotation
    String cacheKey = cache.key();

    // Try to get value from cache
    Optional<Object> cachedValue = cachePort.get(cacheKey);
    if (cachedValue.isPresent()) {
      return cachedValue.get();
    }

    // Execute the method if cache miss
    Object result = joinPoint.proceed();

    // Cache the result if it's not null
    if (result != null) {
      cachePort.put(cacheKey, result, cache.ttl());
    }

    return result;
  }

}