package org.atlas.infrastructure.notification.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.util.UUIDGenerator;
import org.atlas.infrastructure.notification.sse.controller.SseController;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseAdapter<K> implements InitializingBean, SsePort<K> {

  private final List<SseController<K>> sseControllers;
  private Map<RealtimeNotificationType, SseController<K>> sseControllersCache;

  @Override
  public void afterPropertiesSet() throws Exception {
    sseControllersCache = sseControllers.stream()
        .collect(Collectors.toMap(SseController::getEventType, Function.identity()));
  }

  @Override
  public void notify(SseNotification<K> notification) {
    log.info("Notifying {}", notification);

    SseController<K> sseController = sseControllersCache.get(notification.getType());
    if (sseController == null) {
      throw new IllegalStateException(
          "No SseController found for notification type: " + notification.getType());
    }

    SseEmitter sseEmitter = sseController.getSseEmitter(notification.getKey());
    if (sseEmitter == null) {
      throw new IllegalStateException("Not found SseEmitter for key " + notification.getKey());
    }

    try {
      SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
          .id(UUIDGenerator.generate())
          .name(notification.getType().name())
          .data(notification.getPayload());
      sseEmitter.send(eventBuilder);
      log.info("Notified {}", notification);
    } catch (IOException e) {
      log.error("Failed to notify {}", notification, e);
      sseEmitter.completeWithError(e);
    }
  }
}
