package org.atlas.common.infrastructure.notification.inapp.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.notification.inapp.InAppService;
import org.atlas.common.framework.notification.inapp.SendInAppRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseInAppService implements InAppService {

  private final SseController sseController;

  @Override
  public void send(SendInAppRequest request) {
    log.info("Sending in-app notification to user {}: {}",
        request.getReceiverUserId(), request.getPayload());

    // Create SSE event with userId as the key
    SseEvent event = SseEvent.of(request.getReceiverUserId(), request.getPayload());

    // Send the event via SSE
    sseController.sendEvent(event);

    log.debug("Successfully sent in-app notification to user {}", request.getReceiverUserId());
  }
}
