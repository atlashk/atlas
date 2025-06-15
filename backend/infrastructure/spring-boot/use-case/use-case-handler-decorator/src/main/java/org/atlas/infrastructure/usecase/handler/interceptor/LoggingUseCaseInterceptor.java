package org.atlas.infrastructure.usecase.handler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.util.StopWatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class LoggingUseCaseInterceptor implements UseCaseInterceptor {

  // ThreadLocal to store StopWatch per thread
  private static final ThreadLocal<StopWatch> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatch::new);

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    // Start the StopWatch
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.start();

    ContextInfo contextInfo = Contexts.get();
    if (contextInfo == null) {
      log.debug("Anonymous user started handling use case {}",
          useCaseClass.getSimpleName());
    } else {
      log.debug("User {} started handling use case {}",
          contextInfo, useCaseClass.getSimpleName());
    }
  }

  @Override
  public void postHandle(Class<?> useCaseClass, Object input) {
    ContextInfo contextInfo = Contexts.get();

    // Stop the StopWatch and get elapsed time
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.stop();
    long elapsedTimeMs = stopWatch.getElapsedTimeMs();

    // Merge user info and performance check into one log statement
    if (contextInfo == null) {
      log.debug("Anonymous user finished handling use case {} in {} ms",
          useCaseClass.getSimpleName(), elapsedTimeMs);
    } else {
      log.debug("User {} finished handling use case {} in {} ms",
          contextInfo, useCaseClass.getSimpleName(), elapsedTimeMs);
    }
  }
}
