package org.atlas.framework.domain.event.handler;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.handler.interceptor.EventHandlerInterceptor;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
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
  private final LockPort lockPort;

  @Around("@within(org.atlas.framework.domain.event.handler.DomainEventHandler) && execution(* handle(..))")
  public Object aroundHandle(ProceedingJoinPoint joinPoint) throws Throwable {
    // Retrieve input safely
    Object[] args = joinPoint.getArgs();
    DomainEvent event = (DomainEvent) args[0];

    // Execute pre-handle interceptors
    interceptors.forEach(interceptor -> interceptor.preHandle(event));

    Object[] result = new Object[1];
    Throwable[] exception = new Throwable[1];

    // Try acquiring lock and execute the following steps within the lock
    String lockKey = applicationConfigService.getApplicationName() + "::" + event.getEventId();
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofDays(7);

    try {
      // Try to acquire the lock
      boolean lockAcquired = lockPort.acquireLock(lockKey, waitTime, leaseTime);
      if (!lockAcquired) {
        throw new LockAcquisitionException(
            "Failed to acquire lock for event: " + event.getEventId());
      }

      // Handle event within the lock
      result[0] = joinPoint.proceed();
      event.markAsProcessed();
    } catch (Exception e) {
      exception[0] = e;
    }

    // Execute post-handle interceptors (outside locked section)
    try {
      interceptors.forEach(interceptor -> interceptor.postHandle(event));
    } catch (Exception e) {
      log.warn("Error in post-handle interceptor for event {}: {}", event.getEventId(),
          e.getMessage(), e);
      // Don't throw here as the main processing is already done
    }

    // Re-throw any exception that occurred during processing
    if (exception[0] != null) {
      throw exception[0];
    }

    return result[0];
  }
}
