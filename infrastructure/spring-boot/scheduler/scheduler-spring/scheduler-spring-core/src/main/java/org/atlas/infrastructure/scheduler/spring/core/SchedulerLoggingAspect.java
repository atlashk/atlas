package org.atlas.infrastructure.scheduler.spring.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.util.StopWatch;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class SchedulerLoggingAspect {

  @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
  public Object aroundScheduled(ProceedingJoinPoint joinPoint) throws Throwable {
    Class<?> targetClass = joinPoint.getTarget().getClass();
    String taskName = targetClass.getSimpleName();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    log.info("Started executing task {}", taskName);

    Object result = joinPoint.proceed();

    stopWatch.stop();
    long elapsedTimeMs = stopWatch.getElapsedTimeMs();
    log.info("Finished executing task {}. Elapsed time: {} ms", taskName, elapsedTimeMs);

    return result;
  }
}
