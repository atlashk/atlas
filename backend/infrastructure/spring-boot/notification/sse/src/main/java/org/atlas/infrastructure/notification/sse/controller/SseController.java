package org.atlas.infrastructure.notification.sse.controller;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public abstract class SseController<K> {

  protected final ConcurrentHashMap<K, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

  public abstract RealtimeNotificationType getEventType();

  public SseEmitter getSseEmitter(K key) {
    return sseEmitters.get(key);
  }

  protected SseEmitter subscribe(K key) {
    log.debug("Subscribing SseEmitter for key: {}", key);

    // No timeout
    SseEmitter sseEmitter = new SseEmitter(-1L);

    sseEmitter.onCompletion(() -> {
      log.debug("SseEmitter completed for key: {}", key);
      sseEmitters.remove(key);
    });

    sseEmitter.onError(e -> {
      log.error("SseEmitter error for key: {}", key, e);
      sseEmitters.remove(key);
    });

    sseEmitter.onTimeout(() -> {
      log.debug("SseEmitter timed out for key: {}", key);
      sseEmitters.remove(key);
    });

    sseEmitters.put(key, sseEmitter);
    log.debug("Subscribed SseEmitter for key: {}", sseEmitter);

    return sseEmitter;
  }
}
