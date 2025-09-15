package org.atlas.infrastructure.domain.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.transaction.TransactionPort;
import org.atlas.infrastructure.domain.event.handler.interceptor.EventHandlerInterceptor;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class DomainEventHandlerAspect {

  private final List<EventHandlerInterceptor> interceptors;
  private final TransactionPort transactionPort;

  @Around("@within(org.atlas.framework.domain.event.handler.DomainEventHandler) && execution(* handle(..))")
  public Object aroundHandle(ProceedingJoinPoint joinPoint) throws Throwable {
    // Retrieve input safely
    Object[] args = joinPoint.getArgs();
    DomainEvent event = (DomainEvent) args[0];

    // Execute pre-handle interceptors
    interceptors.forEach(interceptor -> interceptor.preHandle(event));

    // Execute EventHandler in a transaction manner
    transactionPort.begin();
    try {
      Object result = joinPoint.proceed();
      transactionPort.commit();
      event.markAsProcessed();
      return result;
    } catch (Throwable e) {
      transactionPort.rollback();
      throw e;
    } finally {
      // Execute post-handle interceptors
      interceptors.forEach(interceptor -> interceptor.postHandle(event));
    }
  }
}
