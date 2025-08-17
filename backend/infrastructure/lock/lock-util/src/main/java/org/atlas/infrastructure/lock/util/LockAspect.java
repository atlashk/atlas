package org.atlas.infrastructure.lock.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.lock.LockPort;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LockAspect {

  private final LockPort lockPort;

  @Around("@annotation(lock)")
  public Object applyLock(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
    String key = lock.key();
    Duration timeout = Duration.of(lock.timeout(), lock.timeUnit().toChronoUnit());

    final Object[] result = new Object[1];

    lockPort.doWithLock(() -> {
      try {
        result[0] = joinPoint.proceed();
      } catch (Throwable ex) {
        throw new RuntimeException(ex); // will be unwrapped in AOP
      }
    }, key, timeout);

    return result[0];
  }
}
