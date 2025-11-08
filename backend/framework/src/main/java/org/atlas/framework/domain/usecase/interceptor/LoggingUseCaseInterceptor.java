package org.atlas.framework.domain.usecase.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.i18n.I18nService;
import org.atlas.framework.util.StopWatchUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class LoggingUseCaseInterceptor implements UseCaseInterceptor {

  private final I18nService i18nService;

  // ThreadLocal to store StopWatch per thread
  private static final ThreadLocal<StopWatchUtil> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatchUtil::new);

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    // Start the StopWatch
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.start();

    String user = Contexts.getUserInfo();

    log.debug("User {} started handling use case {}",
        user, useCaseClass.getSimpleName());
  }

  @Override
  public void postHandle(Class<?> useCaseClass, Object input) {
    // Stop the StopWatch and get elapsed time
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.stop();
    long elapsedTimeMs = stopWatchUtil.getElapsedTimeMs();

    // User info
    String user = Contexts.getUserInfo();

    log.debug("User {} finished handling use case {} in {} ms",
        user, useCaseClass.getSimpleName(), elapsedTimeMs);
  }

  @Override
  public void onError(Class<?> useCaseClass, Object input, Throwable error) {
    // User info
    String user = Contexts.getUserInfo();

    // Error message
    String errorMessage;
    if (error instanceof DomainException ex) {
      errorMessage = i18nService.getMessage(ex.getMessage());
    } else {
      errorMessage = error.getMessage();
    }
    if (errorMessage == null) {
      errorMessage = "Unknown error";
    }

    log.error("User {} encountered error while handling use case {}: {}",
        user, useCaseClass.getSimpleName(), errorMessage, error);
  }
}
