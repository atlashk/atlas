package org.atlas.infrastructure.realtime.sse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.realtime.RealtimeEventType;
import org.atlas.framework.realtime.sse.SseEvent;
import org.atlas.framework.realtime.sse.SseService;
import org.atlas.infrastructure.realtime.sse.controller.SseController;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SSE")
public class SpringSseService implements SseService {

  private final List<SseController> sseControllers;

  @Override
  public <T> void emit(SseEvent<T> event) {
    log.info("Emitting event {}", event);

    // Find the relevant SSE controller
    SseController sseController = findSseController(event.getType())
        .orElseThrow(() -> new IllegalStateException(
            "No SseController found for event type: " + event.getType()));
    log.debug("Using controller {} for event type {}",
        sseController.getClass().getSimpleName(), event.getType());

    // Send event via all emitters of controller
    var sseEmitters = sseController.getSseEmitters(event.getKey());
    if (sseEmitters.isEmpty()) {
      log.warn("No SseEmitters found for key: {}", event.getKey());
      return;
    }

    String payloadJson = JsonUtil.getInstance().toJson(event.getPayload());
    SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
        .id(event.getId())
        .name(event.getType().name())
        .data(payloadJson);

    int successCount = 0;
    int errorCount = 0;

    for (SseEmitter sseEmitter : sseEmitters) {
      try {
        sseEmitter.send(eventBuilder);
        successCount++;
      } catch (IOException e) {
        log.error("Failed to send event to SseEmitter for key: {}", event.getKey(), e);
        sseEmitter.completeWithError(e);
        errorCount++;
      }
    }

    log.info("Emitted event {} - sent to {} emitters: {} succeeded, {} failed",
        event, sseEmitters.size(), successCount, errorCount);
  }

  private Optional<SseController> findSseController(RealtimeEventType eventType) {
    // Simple iteration through all controllers
    for (SseController controller : sseControllers) {
      if (controller.canHandle(eventType)) {
        return Optional.of(controller);
      }
    }
    return Optional.empty();
  }
}
