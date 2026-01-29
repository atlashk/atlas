package org.atlas.services.notification.application.saga.checkout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.concurrent.AsyncUtil;
import org.atlas.libs.framework.concurrent.AsyncUtil.AsyncTask;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.error.ErrorUtil;
import org.atlas.libs.framework.file.FileUtil;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.notification.email.Attachment;
import org.atlas.libs.framework.notification.email.EmailService;
import org.atlas.libs.framework.notification.email.SendEmailException;
import org.atlas.libs.framework.notification.email.SendEmailRequest;
import org.atlas.libs.framework.notification.inapp.InAppService;
import org.atlas.libs.framework.notification.inapp.SendInAppRequest;
import org.atlas.libs.framework.notification.inapp.SendInAppRequest.Payload;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.template.ResolveTemplateException;
import org.atlas.libs.framework.template.TemplateService;
import org.atlas.services.notification.application.service.InAppNotificationService;
import org.atlas.services.notification.application.service.NotificationService;
import org.atlas.services.notification.domain.entity.DeliveryStatus;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.domain.entity.NotificationChannel;
import org.atlas.services.notification.domain.entity.NotificationType;
import org.atlas.services.notification.domain.entity.metadata.OrderFulfilledMetadata;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyOrderFulfilledCommandHandler {

  private final ApplicationConfigService applicationConfigService;
  private final EmailService emailService;
  private final InAppService inAppService;
  private final InAppNotificationService inAppNotificationService;
  private final NotificationService notificationService;
  private final TemplateService templateService;

  @SagaCommandHandler(command = CheckoutCommand.NOTIFY_ORDER_FULFILLED)
  public SagaCommandResult notifyOrderFulfilled(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    AsyncUtil.executeTasks(
        notifyEmail(checkoutSagaData),
        notifyInApp(checkoutSagaData)
    );

    return SagaCommandResult.success();
  }

  private AsyncTask notifyEmail(CheckoutSagaData sagaData) {
    return new AsyncTask() {
      private Notification notification;

      @Override
      public void run() {
        // Create new notification
        OrderFulfilledMetadata metadata = OrderFulfilledMetadata.builder()
            .orderId(sagaData.getOrderId())
            .build();
        notification = Notification.builder()
            .userId(sagaData.getUser().getId())
            .type(NotificationType.ORDER_FULFILLED)
            .channel(NotificationChannel.EMAIL)
            .metadata(JsonUtil.getInstance().toJson(metadata))
            .deliveryStatus(DeliveryStatus.IN_PROGRESS)
            .build();
        notificationService.create(notification);

        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", sagaData);

        // Subject
        String subject;
        try {
          subject = templateService.resolveEmailSubject("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve email subject template", e);
        }

        // Body
        String body;
        try {
          body = templateService.resolveEmailBody("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve email body template", e);
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
                applicationConfigService.getConfig("notification.email.sender"))
            .orElseThrow(() -> new IllegalStateException("email.sender is not configured"));

        SendEmailRequest request = new SendEmailRequest.Builder()
            .setSender(sender)
            .addRecipient(sagaData.getUser().getEmail())
            .setSubject(subject)
            .setBody(body)
            .addAttachment(attachment)
            .setHtml(true)
            .build();
        try {
          emailService.send(request);
        } catch (SendEmailException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onSuccess() {
        log.info("Succeeded to deliver email notification for order fulfilled: orderId={}",
            sagaData.getOrderId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to deliver email notification for order fulfilled: orderId={}, error={}",
            sagaData.getOrderId(), e.getMessage(), e);
      }
    };
  }

  private AsyncTask notifyInApp(CheckoutSagaData sagaData) {
    return new AsyncTask() {
      private Notification notification;

      @Override
      public void run() {
        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", sagaData);

        // Message
        String message;
        try {
          message = templateService.resolveInAppMessage("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve in-app message template", e);
        }

        // Create new notification
        OrderFulfilledMetadata metadata = OrderFulfilledMetadata.builder()
            .orderId(sagaData.getOrderId())
            .build();
        notification = Notification.builder()
            .userId(sagaData.getUser().getId())
            .type(NotificationType.ORDER_FULFILLED)
            .channel(NotificationChannel.IN_APP)
            .message(message)
            .metadata(JsonUtil.getInstance().toJson(metadata))
            .deliveryStatus(DeliveryStatus.IN_PROGRESS)
            .build();
        notificationService.create(notification);

        SendInAppRequest request = SendInAppRequest.builder()
            .receiverUserId(sagaData.getUser().getId())
            .payload(Payload.builder()
                .message(message)
                .deliveredAt(notification.getCreatedAt())
                .build())
            .build();
        inAppService.send(request);
      }

      @Override
      public void onSuccess() {
        if (notification != null) {
          inAppNotificationService.markAsSucceeded(notification);
        }
        log.info("Succeeded to deliver in-app notification for order fulfilled: orderId={}",
            sagaData.getOrderId());
      }

      @Override
      public void onError(Throwable e) {
        if (notification != null) {
          inAppNotificationService.markAsFailed(notification, ErrorUtil.sanitizeErrorMessage(e));
        }
        log.error("Failed to deliver in-app notification for order fulfilled: orderId={}, error={}",
            sagaData.getOrderId(), e.getMessage(), e);
      }
    };
  }
}
