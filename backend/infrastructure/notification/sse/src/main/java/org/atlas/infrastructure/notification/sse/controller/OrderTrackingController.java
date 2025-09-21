package org.atlas.infrastructure.notification.sse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.common.NotificationType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingController extends SseController {

  @Override
  public boolean canHandle(NotificationType notificationType) {
    return notificationType == NotificationType.ORDER_TRACKING;
  }

  @GetMapping(value = "/notification/sse/orders/{orderId}/tracking",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamOrderNotifications(@PathVariable("orderId") Integer orderId) {
    log.info("Client subscribing to order notifications for orderId: {}", orderId);
    return subscribe(orderId.toString());
  }
}
