package org.atlas.infrastructure.notification.sse.controller;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.common.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public abstract class SseController {

  protected final ConcurrentHashMap<String, Set<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

  /**
   * Checks if this controller can handle the given notification type.
   */
  public abstract boolean canHandle(NotificationType notificationType);

  public Set<SseEmitter> getSseEmitters(String key) {
    return sseEmitters.getOrDefault(key, Set.of());
  }

  protected SseEmitter subscribe(String key) {
    log.debug("Subscribing SseEmitter for key: {}", key);

    // No timeout
    SseEmitter sseEmitter = new SseEmitter(-1L);

    sseEmitter.onCompletion(() -> {
      log.debug("SseEmitter completed for key: {}", key);
      removeSseEmitter(key, sseEmitter);
    });

    sseEmitter.onError(e -> {
      log.error("SseEmitter error for key: {}", key, e);
      removeSseEmitter(key, sseEmitter);
    });

    sseEmitter.onTimeout(() -> {
      log.debug("SseEmitter timed out for key: {}", key);
      removeSseEmitter(key, sseEmitter);
    });

    // Add to the set of emitters for this key
    sseEmitters.computeIfAbsent(key, k -> new CopyOnWriteArraySet<>()).add(sseEmitter);
    log.debug("Subscribed SseEmitter for key: {}, total emitters: {}", key,
        sseEmitters.get(key).size());

    return sseEmitter;
  }

  private void removeSseEmitter(String key, SseEmitter sseEmitter) {
    Set<SseEmitter> emitters = sseEmitters.get(key);
    if (emitters != null) {
      emitters.remove(sseEmitter);
      if (emitters.isEmpty()) {
        sseEmitters.remove(key);
        log.debug("Removed empty emitter set for key: {}", key);
      } else {
        log.debug("Removed SseEmitter for key: {}, remaining emitters: {}", key, emitters.size());
      }
    }
  }
}
