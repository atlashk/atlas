package org.atlas.domain.notification.event;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderConfirmedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailNotification;
import org.atlas.framework.notification.email.EmailPort;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;
import org.atlas.framework.notification.realtime.payload.OrderStatusChangedPayload;
import org.atlas.framework.notification.realtime.sse.SseNotification;
import org.atlas.framework.notification.realtime.sse.SsePort;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;
import org.atlas.framework.notification.realtime.websocket.WebSocketPort;
import org.atlas.framework.template.TemplatePort;
import org.atlas.framework.template.exception.ResolveTemplateException;
import org.atlas.framework.util.FileUtil;
import org.atlas.framework.util.UUIDGenerator;

@DomainEventHandler(type = DomainEventType.ORDER_CONFIRMED)
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedEventHandler {

  private final ApplicationConfigPort applicationConfigPort;
  private final EmailPort emailPort;
  private final SsePort<Integer> ssePort;
  private final TemplatePort templatePort;
  private final WebSocketPort webSocketPort;

  public void handle(OrderConfirmedEvent event) {
    CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> {
              try {
                notifyEmail(event);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .exceptionally(e -> {
              log.error("Failed to notify email for event {}", event.getEventId(), e);
              return null;
            }),
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

  private void notifyEmail(OrderConfirmedEvent event) throws IOException {
    // Model
    Map<String, Object> model = new HashMap<>();
    model.put("order", event);

    // Subject
    String subject;
    try {
      subject = templatePort.resolveEmailSubject("order_confirmed", model);
    } catch (Exception e) {
      throw new ResolveTemplateException("Could not resolve subject template", e);
    }

    // Body
    String body;
    try {
      body = templatePort.resolveEmailBody("order_confirmed", model);
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
        .addRecipient(event.getUser().getEmail())
        .setSubject(subject)
        .setBody(body)
        .addAttachment(attachment)
        .setHtml(true)
        .build();
    emailPort.notify(notification);
  }

  private void notifySse(OrderConfirmedEvent event) {
    OrderStatusChangedPayload payload = OrderStatusChangedPayload.from(event);
    SseNotification<Integer> notification = new SseNotification<>(
        UUIDGenerator.generate(),
        RealtimeNotificationType.ORDER_STATUS_CHANGED,
        event.getOrderId(),
        payload);
    ssePort.notify(notification);
  }

  private void notifyWebSocket(OrderConfirmedEvent event) {
    OrderStatusChangedPayload payload = OrderStatusChangedPayload.from(event);
    WebSocketNotification notification = new WebSocketNotification(
        UUIDGenerator.generate(),
        RealtimeNotificationType.ORDER_STATUS_CHANGED,
        payload);
    webSocketPort.notify(notification);
  }
}
