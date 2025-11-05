package org.atlas.domain.notification.saga.checkout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailNotification;
import org.atlas.framework.notification.email.EmailService;
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
import org.atlas.framework.util.FileUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotifyOrderFulfilledCommandHandler {

  private final ApplicationConfigService applicationConfigService;
  private final EmailService emailService;
  private final TemplateService templateService;

  @SagaCommandHandler(command = CheckoutCommand.NOTIFY_ORDER_FULFILLED)
  public SagaCommandResult notifyOrderFulfilled(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    AsyncUtil.executeTasks(notifyEmail(checkoutSagaData))
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
      @Override
      public void run() {
        // Model
        Map<String, Object> model = new HashMap<>();
        model.put("order", sagaData);

        // Subject
        String subject;
        try {
          subject = templateService.resolveEmailSubject("order_fulfilled", model);
        } catch (Exception e) {
          throw new ResolveTemplateException("Could not resolve subject template", e);
        }

        // Body
        String body;
        try {
          body = templateService.resolveEmailBody("order_fulfilled", model);
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
                applicationConfigService.getConfig("notification.email.sender"))
            .orElseThrow(() -> new IllegalStateException("email.sender is not configured"));

        EmailNotification notification = new EmailNotification.Builder()
            .setSender(sender)
            .addRecipient(sagaData.getUser().getEmail())
            .setSubject(subject)
            .setBody(body)
            .addAttachment(attachment)
            .setHtml(true)
            .build();
        emailService.notify(notification);
      }

      @Override
      public void onSuccess() {
        log.info("Email notification for order fulfilled succeeded: orderId={}",
            sagaData.getOrderId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
            sagaData.getOrderId(), ex.getMessage(), ex);
      }
    };
  }
}
