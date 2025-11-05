package org.atlas.domain.order.saga.checkout;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.entity.Order.CancellationReason;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.constant.Services;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.InitializePaymentCommandMetadata;
import org.atlas.framework.saga.checkout.ProcessPaymentCommandMetadata;
import org.atlas.framework.saga.core.annotation.Saga;
import org.atlas.framework.saga.core.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.core.annotation.StartSaga;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.entity.SagaEntity;
import org.atlas.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.framework.util.AsyncUtil;

@Saga(
    sagaName = "checkout",
    description = "Orchestrates the checkout process"
)
@RequiredArgsConstructor
@Slf4j(topic = "checkout.saga")
public class CheckoutSaga {

  private final OrderRepository orderRepository;
  private final SagaOrchestrator sagaOrchestrator;

  @StartSaga
  public void startSaga(SagaEntity saga) {
    sagaOrchestrator.sendSagaCommand(
        saga, CheckoutCommand.RESERVE_PRODUCT, Services.PRODUCT_SERVICE);
  }

  @SagaCommandReplyHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public void handleReserveProductReply(SagaEntity saga, SagaCommandResult sagaCommandResult) {
    // Update order
    Order order = orderRepository.findBySagaId(saga.getId())
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
    Order order = orderRepository.findBySagaId(saga.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    if (sagaCommandResult.isSuccess()) {
      InitializePaymentCommandMetadata metadata = JsonUtil.getInstance().toObject(
          (LinkedHashMap<?, ?>) sagaCommandResult.getMetadata(),
          InitializePaymentCommandMetadata.class);

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
    Order order = orderRepository.findBySagaId(saga.getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));

    ProcessPaymentCommandMetadata metadata = JsonUtil.getInstance().toObject(
        (LinkedHashMap<?, ?>) sagaCommandResult.getMetadata(), ProcessPaymentCommandMetadata.class);

    if (sagaCommandResult.isSuccess()) {
      // Update order status
      order.setStatus(OrderStatus.FULFILLED);

      // Update payment snapshot
      order.getPayment().setPaymentMethod(metadata.getPaymentMethod());
      order.getPayment().setPaymentMethodDetails(metadata.getPaymentMethodDetails());

      orderRepository.update(order);

      // Send the remaining commands
      AsyncUtil.executeTasks(
          () -> sagaOrchestrator.sendSagaCommand(
              saga, CheckoutCommand.CLEAR_CART, Services.USER_SERVICE),
          () -> sagaOrchestrator.sendSagaCommand(
              saga, CheckoutCommand.NOTIFY_ORDER_FULFILLED, Services.NOTIFICATION_SERVICE)
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
}
