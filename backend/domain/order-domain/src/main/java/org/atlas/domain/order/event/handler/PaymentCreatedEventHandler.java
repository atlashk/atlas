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
import org.atlas.framework.domain.event.contract.order.PaymentCreatedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.realtime.payload.OrderTrackingPayload;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;

@DomainEventHandler(type = DomainEventType.PAYMENT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedEventHandler {

  private final OrderRepository orderRepository;
  private final SsePort ssePort;
  private final WebSocketPort webSocketPort;

  public void handle(PaymentCreatedEvent event) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(event.getOrder().getId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));

    // Validate order status
    if (orderEntity.getStatus() != OrderStatus.PRODUCT_RESERVATION_SUCCEEDED) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Mark order as AWAITING_PAYMENT
    orderEntity.setStatus(OrderStatus.AWAITING_PAYMENT);
    orderRepository.update(orderEntity);

    // Notify to channels
    OrderTrackingPayload orderTrackingPayload = OrderTrackingPayload.builder()
        .orderId(orderEntity.getId())
        .orderStatus(orderEntity.getStatus())
        .paymentGatewayData(event.getPaymentGatewayData())
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