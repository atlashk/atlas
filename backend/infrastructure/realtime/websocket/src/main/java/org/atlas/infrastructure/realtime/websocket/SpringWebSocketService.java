package org.atlas.infrastructure.realtime.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.realtime.websocket.WebSocketEvent;
import org.atlas.framework.realtime.websocket.WebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "WebSocket")
public class SpringWebSocketService implements WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public <T> void emit(WebSocketEvent<T> event) {
    log.info("Emitting {}", event);
    try {
      String destination = WebSocketDestinationResolver.resolve(event);
      messagingTemplate.convertAndSend(destination, event);
      log.info("Emitted {}", event);
    } catch (Exception e) {
      log.error("Failed to emit {}", event, e);
    }
  }
}
