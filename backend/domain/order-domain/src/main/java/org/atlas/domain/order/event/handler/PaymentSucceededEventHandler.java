package org.atlas.domain.order.event.handler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.service.OrderAggregator;
import org.atlas.domain.order.service.OrderAggregator.AggregationOptions;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.async.AsyncTask;
import org.atlas.framework.async.AsyncUtil;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.PaymentSucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailNotification;
import org.atlas.framework.notification.email.EmailPort;
import org.atlas.framework.notification.realtime.payload.OrderTrackingPayload;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;
import org.atlas.framework.template.ResolveTemplateException;
import org.atlas.framework.template.TemplatePort;
import org.atlas.framework.util.FileUtil;

@DomainEventHandler(type = DomainEventType.PAYMENT_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class PaymentSucceededEventHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;
  private final ApplicationConfigPort applicationConfigPort;
  private final EmailPort emailPort;
  private final SsePort ssePort;
  private final TemplatePort templatePort;
  private final WebSocketPort webSocketPort;

  public void handle(PaymentSucceededEvent paymentSucceededEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(paymentSucceededEvent.getOrder().getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    // Validate order status
    if (orderEntity.getStatus() != OrderStatus.AWAITING_PAYMENT) {
      throw new DomainException(DomainError.ORDER_INVALID_STATUS);
    }

    // Mark order as FULFILLED
    orderEntity.setStatus(OrderStatus.FULFILLED);
    orderRepository.update(orderEntity);

    // Aggregate order
    orderAggregator.aggregate(
        orderEntity,
        AggregationOptions.builder()
            .loadUsers(true)
            .loadProducts(true)
            .build()
    );

    // Notify to channels
    OrderTrackingPayload orderTrackingPayload = OrderTrackingPayload.builder()
        .orderId(orderEntity.getId())
        .orderStatus(orderEntity.getStatus())
        .build();
    AsyncUtil.executeAsync(List.of(
        notifyEmail(orderEntity),
        notifySse(orderTrackingPayload),
        notifyWebSocket(orderTrackingPayload)
    ));
  }

  private AsyncTask notifyEmail(OrderEntity orderEntity) {
    return new AsyncTask() {
      @Override
      public void run() {
        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", orderEntity);

        // Subject
        String subject;
        try {
          subject = templatePort.resolveEmailSubject("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve subject template", e);
        }

        // Body
        String body;
        try {
          body = templatePort.resolveEmailBody("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve body template", e);
        }

        // Attachments (demo)
        Attachment attachment;
        File attachmentFile;
        try {
          attachmentFile = FileUtil.readResourceFile("email/attachment/coffee.jpg");
        } catch (IOException e) {
          throw new ResolveTemplateException("Could not resolve attachment", e);
        }
        attachment = new Attachment(attachmentFile.getName(), attachmentFile);

        String sender = Optional.ofNullable(
                applicationConfigPort.getConfig(Application.SYSTEM, "email.sender"))
            .orElseThrow(() -> new IllegalStateException("email.sender is not configured"));

        EmailNotification notification = new EmailNotification.Builder()
            .setSender(sender)
            .addRecipient(orderEntity.getUser().getEmail())
            .setSubject(subject)
            .setBody(body)
            .addAttachment(attachment)
            .setHtml(true)
            .build();
        emailPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Email notification for order fulfilled succeeded: orderId={}",
            orderEntity.getId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
            orderEntity.getId(), ex.getMessage(), ex);
      }
    };
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