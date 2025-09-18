package org.atlas.domain.order.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.service.OrderAggregator;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.concurrent.AsyncTask;
import org.atlas.framework.concurrent.ConcurrentUtil;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.sse.payload.OrderCanceledPayload;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;

@DomainEventHandler(type = DomainEventType.ORDER_CANCELED)
@RequiredArgsConstructor
@Slf4j
public class OrderCanceledEventHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;
  private final SsePort<Integer> ssePort;
  private final WebSocketPort webSocketPort;

  public void handle(OrderCanceledEvent event) {
    OrderEntity order = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (order.getStatus() != OrderStatus.CANCELED) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }
    orderAggregator.aggregate(order, false);

    ConcurrentUtil.executeAsync(List.of(
        notifySse(event),
        notifyWebSocket(event)
    ));
  }

  private AsyncTask notifySse(OrderCanceledEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        OrderCanceledPayload payload = new OrderCanceledPayload(event.getCancelReason());
        String payloadStr = JsonUtil.getInstance().toJson(payload);
        SseNotification notification = new SseNotification(NotificationType.ORDER_CANCELED,
            payloadStr);
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

  private AsyncTask notifyWebSocket(OrderCanceledEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        OrderCanceledPayload payload = new OrderCanceledPayload(event.getCancelReason());
        WebSocketNotification notification = new WebSocketNotification(
            NotificationType.ORDER_CANCELED, payload);
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
