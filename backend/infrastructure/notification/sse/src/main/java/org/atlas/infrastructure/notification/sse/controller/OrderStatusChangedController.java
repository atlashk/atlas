package org.atlas.infrastructure.notification.sse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangedController extends SseController<Integer> {

  @Override
  public RealtimeNotificationType getEventType() {
    return RealtimeNotificationType.ORDER_STATUS_CHANGED;
  }

  @GetMapping(value = "/notification/sse/orders/{orderId}/status",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamOrderStatusNotification(@PathVariable("orderId") Integer orderId) {
    return subscribe(orderId);
  }
}
