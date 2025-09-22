package org.atlas.domain.order.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.async.AsyncTask;
import org.atlas.framework.async.AsyncUtil;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.realtime.payload.OrderTrackingPayload;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;

@DomainEventHandler(type = DomainEventType.ORDER_CANCELED)
@RequiredArgsConstructor
@Slf4j
public class OrderCanceledEventHandler {

  private final OrderRepository orderRepository;
  private final SsePort ssePort;
  private final WebSocketPort webSocketPort;

  public void handle(OrderCanceledEvent event) {
    OrderEntity orderEntity = orderRepository.findById(event.getOrder().getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.CANCELED) {
      throw new DomainException(DomainError.ORDER_INVALID_STATUS);
    }

    // Notify to channels
    OrderTrackingPayload orderTrackingPayload = OrderTrackingPayload.builder()
        .orderId(orderEntity.getId())
        .orderStatus(orderEntity.getStatus())
        .cancellationReason(orderEntity.getCancellationReason())
        .build();
    AsyncUtil.executeAsync(List.of(
        notifySse(orderTrackingPayload),
        notifyWebSocket(orderTrackingPayload)
    ));
  }

  private AsyncTask notifySse(OrderTrackingPayload orderTrackingPayload) {
    return new AsyncTask() {
      @Override
      public void run() {
        SseNotification<OrderTrackingPayload> notification = new SseNotification<>(
            NotificationType.ORDER_TRACKING,
            String.valueOf(orderTrackingPayload.getOrderId()),
            orderTrackingPayload
        );
        ssePort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Notified SSE for order {}: status={}",
            orderTrackingPayload.getOrderId(), orderTrackingPayload.getOrderStatus());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Failed to notify SSE for order {}: status={}, error={}",
            orderTrackingPayload.getOrderId(), orderTrackingPayload.getOrderStatus(),
            ex.getMessage(), ex);
      }
    };
  }

  private AsyncTask notifyWebSocket(OrderTrackingPayload orderTrackingPayload) {
    return new AsyncTask() {
      @Override
      public void run() {
        WebSocketNotification<OrderTrackingPayload> notification = new WebSocketNotification<>(
            NotificationType.ORDER_TRACKING,
            orderTrackingPayload
        );
        webSocketPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Notified WebSocket for order {}: status={}",
            orderTrackingPayload.getOrderId(), orderTrackingPayload.getOrderStatus());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Failed to notify WebSocket for order {}: status={}, error={}",
            orderTrackingPayload.getOrderId(), orderTrackingPayload.getOrderStatus(),
            ex.getMessage(), ex);
      }
    };
  }
}
