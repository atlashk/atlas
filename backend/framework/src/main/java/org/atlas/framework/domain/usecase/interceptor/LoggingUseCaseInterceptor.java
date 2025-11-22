package org.atlas.framework.domain.usecase.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.i18n.I18nService;
import org.atlas.framework.measurement.StopWatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class LoggingUseCaseInterceptor implements UseCaseInterceptor {

  private final I18nService i18nService;

  // ThreadLocal to store StopWatch per thread
  private static final ThreadLocal<StopWatch> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatch::new);

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    // Start the StopWatch
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.start();

    String user = Contexts.getUserInfo();

    log.debug("User {} started handling use case {}",
        user, useCaseClass.getSimpleName());
  }

  @Override
  public void postHandle(Class<?> useCaseClass, Object input) {
    // Stop the StopWatch and get elapsed time
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.stop();
    long elapsedTimeMs = stopWatch.getElapsedTimeMs();

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
