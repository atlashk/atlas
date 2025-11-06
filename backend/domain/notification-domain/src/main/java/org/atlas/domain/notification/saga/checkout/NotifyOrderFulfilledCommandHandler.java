package org.atlas.domain.notification.saga.checkout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.notification.entity.DeliveryStatus;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.domain.notification.entity.NotificationType;
import org.atlas.domain.notification.entity.metadata.OrderFulfilledMetadata;
import org.atlas.domain.notification.repository.NotificationRepository;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailService;
import org.atlas.framework.notification.email.SendEmailException;
import org.atlas.framework.notification.email.SendEmailRequest;
import org.atlas.framework.notification.inapp.InAppService;
import org.atlas.framework.notification.inapp.SendInAppRequest;
import org.atlas.framework.notification.inapp.SendInAppRequest.Payload;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.template.ResolveTemplateException;
import org.atlas.framework.template.TemplateService;
import org.atlas.framework.util.AsyncUtil;
import org.atlas.framework.util.AsyncUtil.AsyncTask;
import org.atlas.framework.util.DateUtil;
import org.atlas.framework.util.ErrorUtil;
import org.atlas.framework.util.FileUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyOrderFulfilledCommandHandler {

  private final NotificationRepository notificationRepository;
  private final ApplicationConfigService applicationConfigService;
  private final EmailService emailService;
  private final InAppService inAppService;
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
        )
        .whenComplete((result, error) -> {
          if (error == null) {
            log.info("Successfully notified for order fulfilled: sagaId={}, orderId={}",
                sagaCommand.getSagaId(), checkoutSagaData.getOrderId());
          } else {
            log.error("Failed to notify for order fulfilled: sagaId={}, orderId={}, error={}",
                sagaCommand.getSagaId(), checkoutSagaData.getOrderId(), error.getMessage());
          }
        });

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
        notificationRepository.insert(notification);

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
        if (notification != null) {
          notification.setDeliveryStatus(DeliveryStatus.SUCCEEDED);
          notification.setDeliveredAt(DateUtil.now());
          notificationRepository.update(notification);
        }
        log.info("Email notification for order fulfilled succeeded: orderId={}",
            sagaData.getOrderId());
      }

      @Override
      public void onError(Throwable e) {
        if (notification != null) {
          notification.setDeliveryStatus(DeliveryStatus.FAILED);
          notification.setDeliveryError(ErrorUtil.sanitizeErrorMessage(e));
          notificationRepository.update(notification);
        }
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
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
        model.put("orderCode", sagaData.getOrderCode());
        model.put("amount", sagaData.getAmount());

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
        notificationRepository.insert(notification);

        SendInAppRequest request = SendInAppRequest.builder()
            .receiverUserId(sagaData.getUser().getId())
            .payload(Payload.builder()
                .message(message)
                .notifiedAt(notification.getCreatedAt())
                .build())
            .build();
        inAppService.send(request);
      }

      @Override
      public void onSuccess() {
        if (notification != null) {
          notification.setDeliveryStatus(DeliveryStatus.SUCCEEDED);
          notification.setDeliveredAt(DateUtil.now());
          notificationRepository.update(notification);
        }
        log.info("In-App notification for order fulfilled succeeded: orderId={}",
            sagaData.getOrderId());
      }

      @Override
      public void onError(Throwable e) {
        if (notification != null) {
          notification.setDeliveryStatus(DeliveryStatus.FAILED);
          notification.setDeliveryError(ErrorUtil.sanitizeErrorMessage(e));
          notificationRepository.update(notification);
        }
        log.error("In-App notification for order fulfilled failed: orderId={}, error={}",
            sagaData.getOrderId(), e.getMessage(), e);
      }
    };
  }
}
