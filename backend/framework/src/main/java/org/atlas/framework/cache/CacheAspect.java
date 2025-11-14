package org.atlas.framework.cache;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.spel.SpelParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Aspect to handle caching functionality for methods annotated with @Cache.
 * <p>
 * This aspect intercepts method calls and implements caching logic:
 * <ol>
 *  <li>Check if cached value exists for the given key. </li>
 *  <li>If exists, return cached value.</li>
 *  <li>If not exists, execute method and cache the result.</li>
 * </ol>
 */
@Component
@Aspect
@ConditionalOnBean(CacheService.class)
@RequiredArgsConstructor
@Slf4j
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
