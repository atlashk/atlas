package org.atlas.domain.notification.event;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;
import org.atlas.framework.notification.realtime.payload.OrderStatusChangedPayload;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;
import org.atlas.framework.util.UUIDGenerator;

@DomainEventHandler(type = DomainEventType.ORDER_CANCELED)
@RequiredArgsConstructor
@Slf4j
public class OrderCanceledEventHandler {

  private final SsePort<Integer> ssePort;
  private final WebSocketPort webSocketPort;

  public void handle(OrderCanceledEvent event) {
    CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> notifySse(event))
            .exceptionally(e -> {
              log.error("Failed to notify SSE for event {}", event.getEventId(), e);
              return null;
            }),
        CompletableFuture.runAsync(() -> notifyWebSocket(event))
            .exceptionally(e -> {
              log.error("Failed to notify WebSocket for event {}", event.getEventId(), e);
              return null;
            })
    ).join();
  }

  private void notifySse(OrderCanceledEvent event) {
    OrderStatusChangedPayload payload = OrderStatusChangedPayload.from(event);
    SseNotification<Integer> notification = new SseNotification<>(
        UUIDGenerator.generate(),
        RealtimeNotificationType.ORDER_STATUS_CHANGED,
        event.getOrderId(),
        payload);
    ssePort.notify(notification);
  }

  private void notifyWebSocket(OrderCanceledEvent event) {
    OrderStatusChangedPayload payload = OrderStatusChangedPayload.from(event);
    WebSocketNotification notification = new WebSocketNotification(
        UUIDGenerator.generate(),
        RealtimeNotificationType.ORDER_STATUS_CHANGED,
        payload);
    webSocketPort.notify(notification);
  }
}
