package org.atlas.infrastructure.notification.inapp.websocket;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.notification.inapp.InAppService;
import org.atlas.framework.notification.inapp.SendInAppRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringWebSocketService implements InAppService {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void send(SendInAppRequest request) {
    WebSocketEvent event = WebSocketEvent.of(request.getPayload());
    String destination = "/inapp/" + request.getReceiverUserId();
    messagingTemplate.convertAndSend(destination, event);
  }
}
