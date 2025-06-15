package org.atlas.infrastructure.event.handler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.util.StopWatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class LoggingEventHandlerInterceptor implements EventHandlerInterceptor {

  private static final ThreadLocal<StopWatch> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatch::new);

  @Override
  public void preHandle(DomainEvent event) {
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.start();
    log.debug("Started handling event {}", event);
  }

  @Override
  public void postHandle(DomainEvent event) {
    StopWatch stopWatch = STOP_WATCH_THREAD_LOCAL.get();
    stopWatch.stop();
    long elapsedTimeMs = stopWatch.getElapsedTimeMs();
    log.debug("Finished handling event {}. Elapsed time: {} ms",
        event.getEventId(), elapsedTimeMs);
    // Clean up: Reset the StopWatch and remove it from ThreadLocal
    STOP_WATCH_THREAD_LOCAL.remove();
  }
}
