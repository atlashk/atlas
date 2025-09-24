package org.atlas.framework.domain.usecase.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.util.StopWatchUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class LoggingUseCaseInterceptor implements UseCaseInterceptor {

  // ThreadLocal to store StopWatch per thread
  private static final ThreadLocal<StopWatchUtil> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatchUtil::new);

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    // Start the StopWatch
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.start();

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
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.stop();
    long elapsedTimeMs = stopWatchUtil.getElapsedTimeMs();

    // Merge user info and performance check into one log statement
    if (contextInfo == null) {
      log.debug("Anonymous user finished handling use case {} in {} ms",
          useCaseClass.getSimpleName(), elapsedTimeMs);
    } else {
      log.debug("User {} finished handling use case {} in {} ms",
          contextInfo, useCaseClass.getSimpleName(), elapsedTimeMs);
    }
  }

  @Override
  public void onError(Class<?> useCaseClass, Object input, Throwable error) {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo == null) {
      log.error("Anonymous user encountered error while handling use case {}: {}",
          useCaseClass.getSimpleName(), error.getMessage(), error);
    } else {
      log.error("User {} encountered error while handling use case {}: {}",
          contextInfo, useCaseClass.getSimpleName(), error.getMessage(), error);
    }
  }
}
