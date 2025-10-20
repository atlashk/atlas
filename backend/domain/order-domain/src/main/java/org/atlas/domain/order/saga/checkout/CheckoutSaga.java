package org.atlas.domain.order.saga.checkout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.aggregator.OrderAggregator;
import org.atlas.domain.order.aggregator.OrderAggregator.AggregationOptions;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.async.AsyncTask;
import org.atlas.framework.async.AsyncUtil;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.constant.Services;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailNotification;
import org.atlas.framework.notification.email.EmailService;
import org.atlas.framework.saga.annotation.Saga;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.annotation.StartSaga;
import org.atlas.framework.saga.command.SagaCommandResult;
import org.atlas.framework.saga.command.model.CheckoutCommand;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.context.model.CheckoutSagaData;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.orchestrator.SagaOrchestrator;
import org.atlas.framework.template.ResolveTemplateException;
import org.atlas.framework.template.TemplateService;
import org.atlas.framework.util.FileUtil;

@Saga(
    sagaName = "checkout",
    description = "Orchestrates the checkout process"
)
@RequiredArgsConstructor
@Slf4j(topic = "checkout.saga")
public class CheckoutSaga {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;
  private final ApplicationConfigService applicationConfigService;
  private final SagaOrchestrator sagaOrchestrator;
  private final EmailService emailService;
  private final TemplateService templateService;

  @StartSaga
  public void startSaga(SagaEntity sagaEntity) {
    sagaOrchestrator.sendCommand(
        sagaEntity, CheckoutCommand.CREATE_ORDER, Services.ORDER_SERVICE);
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.CREATE_ORDER)
  public void handleCreateOrderReply(SagaEntity sagaEntity, SagaCommandResult sagaCommandResult) {
    // Update context
    SagaContext sagaContext = SagaContext.deserialize(sagaEntity.getContext());
    sagaContext.remove("input");
    sagaContext.put("data", sagaCommandResult.getResult());
    sagaOrchestrator.syncSagaContext(sagaEntity.getId(), sagaContext);

    sagaOrchestrator.sendCommand(
        sagaEntity, CheckoutCommand.RESERVE_PRODUCT, Services.PRODUCT_SERVICE);
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public void handleReserveProductReply(SagaEntity sagaEntity,
      SagaCommandResult sagaCommandResult) {
    // Update order
    OrderEntity order = orderRepository.findBySagaId(sagaEntity.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    if (sagaCommandResult.isSuccess()) {
      order.setStatus(OrderStatus.AWAITING_PAYMENT_INITIALIZED);
    } else {
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(sagaCommandResult.getErrorMessage());
    }
    orderRepository.update(order);

    sagaOrchestrator.sendCommand(
        sagaEntity, CheckoutCommand.INITIALIZE_PAYMENT, Services.PAYMENT_SERVICE);
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public void handleInitializePaymentReply(SagaEntity sagaEntity,
      SagaCommandResult sagaCommandResult) {
    // Update order
    OrderEntity order = orderRepository.findBySagaId(sagaEntity.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    if (sagaCommandResult.isSuccess()) {
      order.setStatus(OrderStatus.AWAITING_PAYMENT_PROCESSED);
      orderRepository.update(order);

      // Explicitly create command for processing payment
      sagaOrchestrator.createCommand(
          sagaEntity.getId(), CheckoutCommand.PROCESS_PAYMENT, Services.EXTERNAL_PAYMENT_SERVICE);
    } else {
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(sagaCommandResult.getErrorMessage());
      orderRepository.update(order);
    }
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.PROCESS_PAYMENT)
  public void handleProcessPaymentReply(SagaEntity sagaEntity) {
    // Update order status
    SagaContext sagaContext = SagaContext.deserialize(sagaEntity.getContext());
    CheckoutSagaData checkoutSagaData = getCheckoutSagaData(sagaContext);
    OrderEntity order = orderRepository.findById(checkoutSagaData.getOrderId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    order.setStatus(OrderStatus.FULFILLED);
    orderRepository.update(order);

    // Notify to channels
    orderAggregator.aggregate(
        order,
        AggregationOptions.builder()
            .loadUsers(true)
            .loadProducts(true)
            .build()
    );
    AsyncUtil.executeAsync(notifyEmail(order));

    sagaOrchestrator.endSaga(sagaEntity.getId());
  }

  private CheckoutSagaData getCheckoutSagaData(SagaContext sagaContext) {
    CheckoutSagaData checkoutSagaData = sagaContext.get("order", CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }
    return checkoutSagaData;
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
            .addRecipient(order.getUser().getEmail())
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
            order.getId());
      }

      @Override
      public void onError(Throwable ex) {
        log.error("Email notification for order fulfilled failed: orderId={}, error={}",
            order.getId(), ex.getMessage(), ex);
      }
    };
  }
}
