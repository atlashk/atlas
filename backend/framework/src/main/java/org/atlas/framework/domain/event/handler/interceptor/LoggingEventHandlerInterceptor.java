package org.atlas.framework.domain.event.handler.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.util.StopWatchUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class LoggingEventHandlerInterceptor implements EventHandlerInterceptor {

  private static final ThreadLocal<StopWatchUtil> STOP_WATCH_THREAD_LOCAL =
      ThreadLocal.withInitial(StopWatchUtil::new);

  @Override
  public void preHandle(DomainEvent event) {
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.start();
    log.debug("Started handling event {}", event);
  }

  @Override
  public void postHandle(DomainEvent event) {
    StopWatchUtil stopWatchUtil = STOP_WATCH_THREAD_LOCAL.get();
    stopWatchUtil.stop();
    long elapsedTimeMs = stopWatchUtil.getElapsedTimeMs();
    log.debug("Finished handling event {}. Elapsed time: {} ms",
        event.getEventId(), elapsedTimeMs);
    // Clean up: Reset the StopWatch and remove it from ThreadLocal
    STOP_WATCH_THREAD_LOCAL.remove();
  }
}
