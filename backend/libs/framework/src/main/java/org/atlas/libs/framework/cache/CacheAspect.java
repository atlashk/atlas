package org.atlas.libs.framework.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.atlas.libs.framework.spel.SpelParser;
import org.atlas.libs.framework.util.JsonUtil;
import org.springframework.beans.factory.ObjectProvider;
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
@Slf4j
public class CacheAspect {

  private final ObjectProvider<CacheService> cacheServiceProvider;
  private final SpelParser spelParser;

  /**
   * Around advice for methods annotated with @Cache. Implements cache-aside pattern.
   */
  @Around("@annotation(cache)")
  public Object handleCaching(ProceedingJoinPoint joinPoint, Cache cache) throws Throwable {
    // If CacheService is not available (e.g., due to KvStoreService missing), skip caching gracefully
    CacheService cacheService = cacheServiceProvider.getIfAvailable();
    if (cacheService == null) {
      // No cache backend available; just proceed without caching
      return joinPoint.proceed();
    }

    // Evaluate SpEL
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    String cacheKey = Cache.DEFAULT_KEY.equals(cache.key())
        ? Cache.DEFAULT_KEY
        : spelParser.parse(cache.key(), method, joinPoint.getArgs());

    // Cache-aside pattern
    Type genericReturnType = method.getGenericReturnType();
    Optional<Object> cachedValueOpt = cacheService.get(cache.name(), cacheKey, Object.class);
    if (cachedValueOpt.isPresent()) {
      Object cachedValue = cachedValueOpt.get();
      // Rehydrate cached JSON-compatible data into the exact generic return type of the intercepted method.
      return JsonUtil.toObject(cachedValue, genericReturnType);
    } else {
      Object result = joinPoint.proceed();
      if (result != null) {
        if (cache.ttl() == 0L) {
          cacheService.put(cache.name(), cacheKey, result);
        } else {
          cacheService.put(cache.name(), cacheKey, result, cache.ttl());
        }
      }
      return result;
    }
  }
}
