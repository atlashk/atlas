package org.atlas.framework.domain.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.domain.usecase.interceptor.UseCaseInterceptor;
import org.atlas.framework.util.ArrayUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect that handles UseCase execution with interceptors. This aspect runs with the highest
 * priority (before @Transactional) to ensure UseCaseInterceptors run before and after transaction
 * boundaries.
 */
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
// Highest priority to run before @Transactional (which has LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class UseCaseHandlerAspect {

  private final List<UseCaseInterceptor> useCaseInterceptors;

  @Around("(@within(org.atlas.framework.domain.usecase.UseCaseHandler) || " +
         "@within(org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler)) && " +
         "execution(* handle(..))")
  public Object aroundHandle(ProceedingJoinPoint joinPoint) throws Throwable {
    // Extract class and method details
    Class<?> useCaseHandlerClass = joinPoint.getTarget().getClass();
    Object[] args = joinPoint.getArgs();
    Object input = ArrayUtil.isNotEmpty(args) ? args[0] : null;

    // Execute pre-handle interceptors (before transaction)
    useCaseInterceptors.forEach(interceptor ->
        interceptor.preHandle(useCaseHandlerClass, input));

    try {
      // This will trigger @Transactional and execute the method within transaction
      Object result = joinPoint.proceed();

      // Execute post-handle interceptors (after transaction)
      useCaseInterceptors.forEach(interceptor ->
          interceptor.postHandle(useCaseHandlerClass, input));

      return result;
    } catch (Throwable e) {
      // Execute error interceptors (after transaction rollback)
      useCaseInterceptors.forEach(interceptor ->
          interceptor.onError(useCaseHandlerClass, input, e));

      throw e;
    }
  }
}
