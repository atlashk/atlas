package org.atlas.libs.notification.inapp.sse;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse/inapp")
@Slf4j
public class SseController {

  private final ConcurrentHashMap<String, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

  @GetMapping("/{userId}")
  public SseEmitter subscribe(@PathVariable String userId) {
    log.info("User {} subscribing to in-app notifications", userId);

    SseEmitter emitter = new SseEmitter(-1L); // No timeout

    emitter.onCompletion(() -> {
      log.debug("SseEmitter completed for user: {}", userId);
      removeEmitter(userId, emitter);
    });

    emitter.onError(e -> {
      log.error("SseEmitter error for user: {}", userId, e);
      removeEmitter(userId, emitter);
    });

    emitter.onTimeout(() -> {
      log.debug("SseEmitter timed out for user: {}", userId);
      removeEmitter(userId, emitter);
    });

    // Add emitter to the user's emitter set
    userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(emitter);

    log.info("User {} subscribed successfully, total emitters: {}",
        userId, userEmitters.get(userId).size());

    return emitter;
  }

  public void sendEvent(SseEvent event) {
    String userId = event.getEventKey();
    Set<SseEmitter> emitters = userEmitters.get(userId);

    if (emitters == null || emitters.isEmpty()) {
      log.warn("No active SSE connections for user: {}", userId);
      return;
    }

    log.info("Sending in-app notification to user {}: {}", userId, event.getPayload());

    int successCount = 0;
    int errorCount = 0;

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .id(event.getEventId())
            .name("inapp-notification")
            .data(event.getPayload()));
        successCount++;
      } catch (IOException e) {
        log.error("Failed to send notification to user: {}", userId, e);
        emitter.completeWithError(e);
        errorCount++;
      }
    }

    log.info("Notification sent to user {} - {} emitters succeeded, {} failed",
        userId, successCount, errorCount);
  }

  private void removeEmitter(String userId, SseEmitter emitter) {
    Set<SseEmitter> emitters = userEmitters.get(userId);
    if (emitters != null) {
      emitters.remove(emitter);
      if (emitters.isEmpty()) {
        userEmitters.remove(userId);
        log.debug("Removed empty emitter set for user: {}", userId);
      } else {
        log.debug("Removed emitter for user: {}, remaining emitters: {}", userId, emitters.size());
      }
    }
  }
}