package org.atlas.services.order.application.order.saga.checkout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.async.AsyncUtil;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.constant.Services;
import org.atlas.libs.framework.context.ContextInfo;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.file.FileUtil;
import org.atlas.libs.framework.json.jackson.JacksonService;
import org.atlas.libs.framework.notification.email.Attachment;
import org.atlas.libs.framework.notification.email.EmailService;
import org.atlas.libs.framework.notification.email.SendEmailRequest;
import org.atlas.libs.framework.notification.inapp.InAppService;
import org.atlas.libs.framework.notification.inapp.SendInAppRequest;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.InitializePaymentCommandMetadata;
import org.atlas.libs.framework.saga.checkout.ProcessPaymentCommandMetadata;
import org.atlas.libs.framework.saga.core.annotation.Saga;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandReplyHandler;
import org.atlas.libs.framework.saga.core.annotation.StartSaga;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.entity.SagaEntity;
import org.atlas.libs.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.libs.framework.template.ResolveTemplateException;
import org.atlas.libs.framework.template.TemplateService;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.domain.entity.OrderEntity.CancellationReason;
import org.atlas.services.order.domain.error.DomainError;
import org.atlas.services.order.domain.exception.DomainException;
import org.atlas.services.order.port.in.cart.service.CartService;
import org.atlas.services.order.port.out.repository.OrderRepository;

@Saga(
    sagaName = "checkout",
    description = "Orchestrates the checkout process"
)
@RequiredArgsConstructor
@Slf4j(topic = "checkout.saga")
public class CheckoutSaga {

  private final OrderRepository orderRepository;
  private final SagaOrchestrator sagaOrchestrator;
  private final CartService cartService;
  private final ApplicationConfigService applicationConfigService;
  private final EmailService emailService;
  private final InAppService inAppService;
  private final TemplateService templateService;

  @StartSaga
  public void startSaga(SagaEntity saga) {
    sagaOrchestrator.sendSagaCommand(
        saga, CheckoutCommand.RESERVE_STOCK, Services.INVENTORY_SERVICE);
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.RESERVE_STOCK)
  public void handleReserveStockReply(SagaEntity saga, SagaCommandResult sagaCommandResult) {
    // Update order
    OrderEntity order = orderRepository.findBySagaId(saga.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    if (sagaCommandResult.isSuccess()) {
      order.setStatus(OrderStatus.AWAITING_PAYMENT_INITIALIZED);
    } else {
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(
          String.format("%s: %s",
              CancellationReason.FAILED_TO_RESERVE_PRODUCT.getValue(),
              sagaCommandResult.getError()));
    }
    orderRepository.update(order);

    if (sagaCommandResult.isSuccess()) {
      sagaOrchestrator.sendSagaCommand(
          saga, CheckoutCommand.INITIALIZE_PAYMENT, Services.PAYMENT_SERVICE);
    }
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public void handleInitializePaymentReply(SagaEntity saga, SagaCommandResult sagaCommandResult) {
    OrderEntity order = orderRepository.findBySagaId(saga.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    if (sagaCommandResult.isSuccess()) {
      InitializePaymentCommandMetadata metadata = JacksonService.OBJECT_MAPPER.convertValue(
          sagaCommandResult.getMetadata(), InitializePaymentCommandMetadata.class);

      // Update order status
      order.setStatus(OrderStatus.AWAITING_PAYMENT_PROCESSED);

      // Update payment snapshot
      order.getPayment().setTransactionId(metadata.getTransactionId());
      order.getPayment().setPaymentGatewayName(metadata.getPaymentGatewayName());

      orderRepository.update(order);

      // Explicitly create a payment-processing command, since we can’t send commands directly to the external service.
      sagaOrchestrator.createSagaCommand(
          saga.getId(), CheckoutCommand.PROCESS_PAYMENT, Services.EXTERNAL_PAYMENT_SERVICE);
    } else {
      // Update order status
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(
          String.format("%s: %s",
              CancellationReason.FAILED_TO_INITIALIZE_PAYMENT.getValue(),
              sagaCommandResult.getError()));

      orderRepository.update(order);
    }
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.PROCESS_PAYMENT)
  public void handleProcessPaymentReply(SagaEntity saga, SagaCommandResult sagaCommandResult) {
    // Update order
    OrderEntity order = orderRepository.findBySagaId(saga.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    ProcessPaymentCommandMetadata metadata = JacksonService.OBJECT_MAPPER.convertValue(
        sagaCommandResult.getMetadata(), ProcessPaymentCommandMetadata.class);

    if (sagaCommandResult.isSuccess()) {
      // Update order
      order.setStatus(OrderStatus.FULFILLED);
      order.getPayment().setPaymentMethod(metadata.getPaymentMethod());
      order.getPayment().setPaymentMethodDetails(metadata.getPaymentMethodDetails());
      orderRepository.update(order);

      // Do post-payment tasks asynchronously
      AsyncUtil.executeTasks(
          () -> {
            // Confirm stock reservation
            sagaOrchestrator.sendSagaCommand(
                saga, CheckoutCommand.CONFIRM_STOCK_RESERVATION, Services.INVENTORY_SERVICE);
          },
          () -> {
            // Send email
            sendEmailForFulfilledOrder(order);
          },
          () -> {
            // Send in-app notification
            sendInAppNotificationForFulfilledOrder(order);
          },
          () -> {
            // Clear cart
            String userId = order.getUser().getId();
            ContextInfo contextInfo = ContextInfo.builder()
                .userId(userId)
                .build();
            Contexts.set(contextInfo);

            cartService.clearCart();
            log.info("Cleared cart successfully: orderId={}, userId={}", order.getId(), userId);
          }
      );
    } else {
      // Update order status
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(
          String.format("%s: %s",
              CancellationReason.FAILED_TO_PROCESS_PAYMENT.getValue(),
              sagaCommandResult.getError()));

      orderRepository.update(order);
    }

    sagaOrchestrator.endSaga(saga.getId());
  }

  private void sendEmailForFulfilledOrder(OrderEntity order) {
    try {
      // Model
      Map<String, Object> model = new HashMap<>();
      model.put("order", order);

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
          .addRecipient(order.getUser().getEmail())
          .setSubject(subject)
          .setBody(body)
          .addAttachment(attachment)
          .setHtml(true)
          .build();
      emailService.send(request);

      log.info("Sent order fulfilled email: orderId={}, userEmail={}",
          order.getId(), order.getUser().getEmail());
    } catch (Exception e) {
      log.error("Failed to send order fulfilled email: orderId={}, userEmail={}, error={}",
          order.getId(), order.getUser().getEmail(), e.getMessage(), e);
    }
  }

  private void sendInAppNotificationForFulfilledOrder(OrderEntity order) {
    try {
      // Model
      Map<String, Object> model = new HashMap<>();
      model.put("order", order);

      // Message
      String message;
      try {
        message = templateService.resolveInAppMessage("order_fulfilled", model);
      } catch (Exception e) {
        throw new ResolveTemplateException("Could not resolve in-app message template", e);
      }

      SendInAppRequest request = SendInAppRequest.builder()
          .receiverUserId(order.getUser().getId())
          .payload(SendInAppRequest.Payload.builder()
              .message(message)
              .deliveredAt(DateUtil.now())
              .build())
          .build();
      inAppService.send(request);

      log.info("Sent order fulfilled in-app notification: orderId={}, userId={}",
          order.getId(), order.getUser().getId());
    } catch (Exception e) {
      log.error(
          "Failed to send order fulfilled in-app notification: orderId={}, userId={}, error={}",
          order.getId(), order.getUser().getId(), e.getMessage(), e);
    }
  }
}
