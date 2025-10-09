package org.atlas.framework.lock;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LockAspect {

  private final LockService lockService;

  @Around("@annotation(lock)")
  public Object applyLock(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
    String key = lock.key();
    Duration waitTime = Duration.of(lock.waitTime(), lock.timeUnit().toChronoUnit());
    Duration leaseTime = Duration.of(lock.leaseTime(), lock.timeUnit().toChronoUnit());

    // Try to acquire the lock
    boolean lockAcquired = lockService.acquireLock(key, waitTime, leaseTime);
    if (!lockAcquired) {
      throw new RuntimeException("Failed to acquire lock for key: " + key);
    }

    try {
      // Execute the method within the lock
      return joinPoint.proceed();
    } finally {
      // Release the lock if unlockOnCompletion is true
      if (lock.unlockOnCompletion()) {
        try {
          lockService.releaseLock(key);
        } catch (Exception e) {
          log.warn("Failed to release lock for key: {}", key, e);
        }
      }
    }
  }
}
