package org.atlas.infrastructure.notification.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;
import org.atlas.infrastructure.notification.websocket.config.WebSocketDestinationResolver;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SpringWebSocket")
public class SpringWebSocketAdapter implements WebSocketPort {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public <T> void notify(WebSocketNotification<T> notification) {
    log.info("Notifying {}", notification);
    try {
      String destination = WebSocketDestinationResolver.resolve(notification);
      messagingTemplate.convertAndSend(destination, notification);
      log.info("Notified {}", notification);
    } catch (Exception e) {
      log.error("Failed to notify {}", notification, e);
    }
  }
}
