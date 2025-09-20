package org.atlas.infrastructure.domain.event.handler;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.transaction.TransactionPort;
import org.atlas.infrastructure.domain.event.handler.interceptor.EventHandlerInterceptor;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class DomainEventHandlerAspect {

  private final List<EventHandlerInterceptor> interceptors;
  private final ApplicationConfigPort applicationConfigPort;
  private final LockPort lockPort;
  private final TransactionPort transactionPort;

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
    String lockKey = applicationConfigPort.getApplicationName() + "::" + event.getEventId();
    Duration waitTime = Duration.ofSeconds(30);
    Duration leaseTime = Duration.ofDays(7);

    try {
      lockPort.doWithLock(() -> {
        // Begin transaction
        transactionPort.begin();
        try {
          // Handle event
          result[0] = joinPoint.proceed();
          // Transaction commit
          transactionPort.commit();
          event.markAsProcessed();
        } catch (Throwable e) {
          // Transaction rollback if possible
          transactionPort.rollback();
          exception[0] = e;
        }
      }, lockKey, waitTime, leaseTime, false);
    } catch (LockAcquisitionException e) {
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
