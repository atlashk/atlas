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
import org.atlas.framework.domain.event.contract.payment.PaymentCreatedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.notification.common.NotificationType;
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
    OrderEntity orderEntity = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));

    // Validate order status
    if (orderEntity.getStatus() != OrderStatus.PRODUCT_RESERVATION_SUCCEEDED) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order status to AWAITING_PAYMENT
    orderEntity.setStatus(OrderStatus.AWAITING_PAYMENT);
    orderRepository.update(orderEntity);

    // Notify
    AsyncUtil.executeAsync(List.of(
        notifySse(event),
        notifyWebSocket(event)
    ));
  }

  private AsyncTask notifySse(PaymentCreatedEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        SseNotification notification = new SseNotification(
            NotificationType.PAYMENT_CREATED,
            String.valueOf(event.getOrderId()),
            event.getPaymentData()
        );
        ssePort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Notified SSE for event {}", event.getEventId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Failed to notify SSE for event {}", event.getEventId(), ex);
      }
    };
  }

  private AsyncTask notifyWebSocket(PaymentCreatedEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        WebSocketNotification notification = new WebSocketNotification(
            NotificationType.PAYMENT_CREATED,
            event
        );
        webSocketPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Notified WebSocket for event {}", event.getEventId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Failed to notify WebSocket for event {}", event.getEventId(), ex);
      }
    };
  }
}