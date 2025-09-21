package org.atlas.infrastructure.notification.sse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.infrastructure.notification.sse.controller.SseController;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SpringSSE")
public class SpringSseAdapter implements SsePort {

  private final List<SseController> sseControllers;

  @Override
  public <T> void notify(SseNotification<T> notification) {
    log.info("Notifying {}", notification);

    // Find the relevant SSE controller
    SseController sseController = findSseController(notification.getType())
        .orElseThrow(() -> new IllegalStateException(
            "No SseController found for notification type: " + notification.getType()));
    log.debug("Using controller {} for notification type {}",
        sseController.getClass().getSimpleName(), notification.getType());

    // Send event via all emitters of controller
    var sseEmitters = sseController.getSseEmitters(notification.getKey());
    if (sseEmitters.isEmpty()) {
      log.warn("No SseEmitters found for key: {}", notification.getKey());
      return;
    }

    String payloadJson = JsonUtil.getInstance().toJson(notification.getPayload());
    SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
        .id(notification.getId())
        .name(notification.getType().name())
        .data(payloadJson);

    int successCount = 0;
    int errorCount = 0;

    for (SseEmitter sseEmitter : sseEmitters) {
      try {
        sseEmitter.send(eventBuilder);
        successCount++;
      } catch (IOException e) {
        log.error("Failed to send notification to SseEmitter for key: {}", notification.getKey(),
            e);
        sseEmitter.completeWithError(e);
        errorCount++;
      }
    }

    log.info("Notified {} - sent to {} emitters: {} succeeded, {} failed",
        notification, sseEmitters.size(), successCount, errorCount);
  }

  private Optional<SseController> findSseController(NotificationType notificationType) {
    // Simple iteration through all controllers
    for (SseController controller : sseControllers) {
      if (controller.canHandle(notificationType)) {
        return Optional.of(controller);
      }
    }
    return Optional.empty();
  }
}
