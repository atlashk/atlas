package org.atlas.libs.framework.domain.event.handler;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.event.DomainEvent;
import org.atlas.libs.framework.domain.event.handler.interceptor.EventHandlerInterceptor;
import org.atlas.libs.framework.lock.LockAcquisitionException;
import org.atlas.libs.framework.lock.LockService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
// Highest priority to run before @Transactional (which has LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class DomainEventHandlerAspect {

  private final List<EventHandlerInterceptor> interceptors;
  private final ApplicationConfigService applicationConfigService;
  private final ObjectProvider<LockService> lockServiceProvider;

  @Around("@within(org.atlas.libs.framework.domain.event.handler.DomainEventHandler) && execution(* handle(..))")
  public Object aroundHandle(ProceedingJoinPoint joinPoint) throws Throwable {
    // If LockService is not available (e.g., due to KvStoreService missing), skip caching gracefully
    LockService lockService = lockServiceProvider.getIfAvailable();
    if (lockService == null) {
      // No cache backend available; just proceed without caching
      return joinPoint.proceed();
    }

    // Retrieve input safely
    Object[] args = joinPoint.getArgs();
    DomainEvent event = (DomainEvent) args[0];

    // Execute pre-handle interceptors
    interceptors.forEach(interceptor -> interceptor.preHandle(event));

    Object result;

    // Try acquiring lock and execute the following steps within the lock
    String lockKey = applicationConfigService.getApplicationName() + "::" + event.getEventId();
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofDays(7);
    try {
      boolean acquiredLock = lockService.acquireLock(lockKey, waitTime, leaseTime);
      if (!acquiredLock) {
        throw new LockAcquisitionException("Could not acquire lock for key: " + lockKey);
      }
      // Handle event
      result = joinPoint.proceed();
      event.markAsProcessed();
    } finally {
      lockService.releaseLock(lockKey);
    }

    // Execute post-handle interceptors (outside locked section)
    try {
      interceptors.forEach(interceptor -> interceptor.postHandle(event));
    } catch (Exception e) {
      log.warn("Error in post-handle interceptor for event {}: {}", event.getEventId(),
          e.getMessage(), e);
      // Don't throw here as the main processing is already done
    }

    return result;
  }
}
