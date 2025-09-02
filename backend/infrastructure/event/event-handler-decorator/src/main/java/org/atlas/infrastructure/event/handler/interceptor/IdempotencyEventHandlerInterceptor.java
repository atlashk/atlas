package org.atlas.infrastructure.event.handler.interceptor;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.kv.KvConfig;
import org.atlas.framework.kv.KvPort;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyEventHandlerInterceptor implements EventHandlerInterceptor {

  private static final String PROCESSING_STATUS = "processing";
  private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(15);
  private static final String PROCESSED_STATUS = "processed";
  private static final Duration PROCESSED_TTL = Duration.ofDays(7);

  private final ApplicationConfigPort applicationConfigPort;
  private final KvPort kvPort;
  private final KvConfig kvConfig;

  @Override
  public void preHandle(DomainEvent event) {
    String eventKey = obtainEventKey(event);

    // Check if the event has already been processed
    String currentStatus = kvPort.get(kvConfig.getEventStoreName(), eventKey)
        .map(String.class::cast)
        .orElse(null);
    if (PROCESSED_STATUS.equals(currentStatus)) {
      throw new IllegalStateException(
          String.format("Event %s has already been processed", event.getEventId()));
    }

    // Try to acquire a processing lock
    boolean lockAcquired = kvPort.putIfAbsent(kvConfig.getEventStoreName(), eventKey,
        PROCESSING_STATUS, PROCESSING_TIMEOUT);
    if (!lockAcquired) {
      // If the key already exists, it means other instance is processing or has processed the event
      throw new IllegalStateException(
          String.format("Event %s is already being processed by other instance",
              event.getEventId()));
    }
  }

  @Override
  public void postHandle(DomainEvent event) {
    String eventKey = obtainEventKey(event);

    if (event.isProcessed()) {
      kvPort.put(kvConfig.getEventStoreName(), eventKey, PROCESSED_STATUS, PROCESSED_TTL);
    } else {
      // Release the processing lock if event was not processed successfully
      kvPort.delete(kvConfig.getEventStoreName(), eventKey);
    }
  }

  private String obtainEventKey(DomainEvent event) {
    return applicationConfigPort.getApplicationName() + "::" + event.getEventId();
  }
}
