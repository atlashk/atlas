package org.atlas.domain.order.event;

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
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.concurrent.AsyncTask;
import org.atlas.framework.concurrent.ConcurrentUtil;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderFulfilledEvent;
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

@DomainEventHandler(type = DomainEventType.ORDER_FULFILLED)
@RequiredArgsConstructor
@Slf4j
public class OrderFulfilledEventHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;
  private final ApplicationConfigPort applicationConfigPort;
  private final EmailPort emailPort;
  private final SsePort<Integer> ssePort;
  private final TemplatePort templatePort;
  private final WebSocketPort webSocketPort;

  public void handle(OrderFulfilledEvent event) {
    OrderEntity order = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (order.getStatus() != OrderStatus.FULFILLED) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }
    orderAggregator.aggregate(order, false);

    ConcurrentUtil.executeAsync(List.of(
        notifyEmail(order),
        notifySse(order),
        notifyWebSocket(order)
    ));
  }

  private AsyncTask notifyEmail(OrderEntity order) {
    return new AsyncTask() {
      @Override
      public void run() {
        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", order);

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
                applicationConfigPort.getConfig(Application.NOTIFICATION_SERVICE, "email.sender"))
            .orElseThrow(() -> new IllegalStateException("email.sender is not configured"));

        EmailNotification notification = new EmailNotification.Builder()
            .setSender(sender)
            .addRecipient(order.getUser().getEmail())
            .setSubject(subject)
            .setBody(body)
            .addAttachment(attachment)
            .setHtml(true)
            .build();
        emailPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Email notification for order fulfilled succeeded: orderId={}", order.getId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
            order.getId(), ex.getMessage(), ex);
      }
    };
  }

  private AsyncTask notifySse(OrderEntity order) {
    return new AsyncTask() {
      @Override
      public void run() {
        SseNotification notification = new SseNotification(NotificationType.ORDER_FULFILLED, null);
        ssePort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("SSE notification for order fulfilled succeeded: orderId={}", order.getId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("SSE notification for order fulfilled failed: orderId={}, error={}",
            order.getId(), ex.getMessage(), ex);
      }
    };
  }

  private AsyncTask notifyWebSocket(OrderEntity order) {
    return new AsyncTask() {
      @Override
      public void run() {
        WebSocketNotification notification = new WebSocketNotification(NotificationType.ORDER_FULFILLED, null);
        webSocketPort.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("WebSocket notification for order fulfilled succeeded: orderId={}", order.getId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("WebSocket notification for order fulfilled failed: orderId={}, error={}",
            order.getId(), ex.getMessage(), ex);
      }
    };
  }
}
