package org.atlas.framework.cache;

import java.lang.reflect.Method;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.atlas.framework.spel.SpelParser;
import org.springframework.stereotype.Component;

/**
 * Aspect to handle caching functionality for methods annotated with @Cache.
 * <p>
 * This aspect intercepts method calls and implements caching logic: 1. Check if cached value exists
 * for the given key 2. If exists, return cached value 3. If not exists, execute method and cache
 * the result
 */
@Component
@Aspect
@RequiredArgsConstructor
public class CacheAspect {

  private final CacheService cacheService;
  private final SpelParser spelParser;

  /**
   * Around advice for methods annotated with @Cache. Implements cache-aside pattern.
   */
  @Around("@annotation(cache)")
  public Object handleCaching(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
    // Evaluate SpEL
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    String cacheKey = spelParser.parse(cache.key(), method, joinPoint.getArgs());

    // Cache-aside pattern
    Optional<Object> cachedValue = cacheService.get(cache.cacheName(), cacheKey);
    if (cachedValue.isPresent()) {
      return cachedValue.get();
    }

    Object result = joinPoint.proceed();

    if (result != null) {
      cacheService.put(cache.cacheName(), cacheKey, result, cache.ttl());
    }

    return result;
  }
}
