package org.atlas.common.framework.cache;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.spel.SpelParser;
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
    String cacheKey = spelParser.parse(cache.key(), method, joinPoint.getArgs());

    ApplicationCache applicationCache = ApplicationCache.requireByName(cache.cacheName());

    // Cache-aside pattern
    Optional<Object> cachedValue = cacheService.get(applicationCache, cacheKey);
    if (cachedValue.isPresent()) {
      Object value = cachedValue.get();

      // Get the return type of the method
      Class<?> returnType = signature.getReturnType();

      // If cached value is a LinkedHashMap (from JSON deserialization), convert it to the expected type
      if (value instanceof LinkedHashMap<?, ?> && !returnType.equals(LinkedHashMap.class)) {
        return JsonUtil.getInstance().toObject((LinkedHashMap<?, ?>) value, returnType);
      } else {
        return value;
      }
    }

    Object result = joinPoint.proceed();

    if (result != null) {
      cacheService.put(applicationCache, cacheKey, result, cache.ttl());
    }

    return result;
  }
}