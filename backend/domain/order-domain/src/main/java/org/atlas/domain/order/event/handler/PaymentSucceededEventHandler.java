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
import org.atlas.domain.order.mapper.OrderEventMapper;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.async.AsyncTask;
import org.atlas.framework.async.AsyncUtil;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderFulfilledEvent;
import org.atlas.framework.domain.event.contract.payment.PaymentSucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailNotification;
import org.atlas.framework.notification.email.EmailPort;
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
  private final ApplicationConfigPort applicationConfigPort;
  private final EmailPort emailPort;
  private final SsePort ssePort;
  private final TemplatePort templatePort;
  private final WebSocketPort webSocketPort;

  public void handle(PaymentSucceededEvent paymentSucceededEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(paymentSucceededEvent.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));

    // Validate order status
    if (orderEntity.getStatus() != OrderStatus.AWAITING_PAYMENT) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order status to AWAITING_PAYMENT
    orderEntity.setStatus(OrderStatus.FULFILLED);
    orderRepository.update(orderEntity);

    // Notify channels
    OrderFulfilledEvent orderFulfilledEvent = new OrderFulfilledEvent(
        applicationConfigPort.getApplicationName());
    orderFulfilledEvent.setOrder(OrderEventMapper.fromOrderEntity(orderEntity));
    AsyncUtil.executeAsync(List.of(
        notifyEmail(orderFulfilledEvent),
        notifySse(orderFulfilledEvent),
        notifyWebSocket(orderFulfilledEvent)
    ));
  }

  private AsyncTask notifyEmail(OrderFulfilledEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", event.getOrder());

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
            .addRecipient(event.getOrder().getUser().getEmail())
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
            event.getOrder().getOrderId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
            event.getOrder().getOrderId(), ex.getMessage(), ex);
      }
    };
  }

  private AsyncTask notifySse(OrderFulfilledEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        // Create payload with order information
        SseNotification notification = new SseNotification(
            NotificationType.ORDER_FULFILLED,
            String.valueOf(event.getOrder().getOrderId()),
            event
        );
        ssePort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("SSE notification for order fulfilled succeeded: orderId={}",
            event.getOrder().getOrderId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("SSE notification for order fulfilled failed: orderId={}, error={}",
            event.getOrder().getOrderId(), ex.getMessage(), ex);
      }
    };
  }

  private AsyncTask notifyWebSocket(OrderFulfilledEvent event) {
    return new AsyncTask() {
      @Override
      public void run() {
        WebSocketNotification notification = new WebSocketNotification(
            NotificationType.ORDER_FULFILLED, null);
        webSocketPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("WebSocket notification for order fulfilled succeeded: orderId={}",
            event.getOrder().getOrderId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("WebSocket notification for order fulfilled failed: orderId={}, error={}",
            event.getOrder().getOrderId(), ex.getMessage(), ex);
      }
    };
  }
}