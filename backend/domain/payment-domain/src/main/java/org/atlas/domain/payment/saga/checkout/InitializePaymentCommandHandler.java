package org.atlas.domain.payment.saga.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.service.PaymentRoutingService;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.paymentgateway.PaymentGatewayService;
import org.atlas.framework.paymentgateway.model.CreatePaymentRequest;
import org.atlas.framework.paymentgateway.model.CreatePaymentResponse;
import org.atlas.framework.saga.annotation.SagaCommandHandler;
import org.atlas.framework.saga.command.CheckoutCommand;
import org.atlas.framework.saga.command.SagaCommandResult;
import org.atlas.framework.saga.context.CheckoutSagaData;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializePaymentCommandHandler {

  private final PaymentRepository paymentRepository;
  private final PaymentRoutingService paymentRoutingService;
  private final ApplicationConfigService applicationConfigService;

  @SagaCommandHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public SagaCommandResult initializePayment(SagaCommand event) {
    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    CheckoutSagaData checkoutSagaData = sagaContext.get("data", CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Find the relevant payment gateway
    PaymentGatewayService paymentGatewayService = paymentRoutingService.getPaymentGateway(
        checkoutSagaData.getPaymentMethod());

    // Insert new payment entity
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setOrderId(checkoutSagaData.getOrderId());
    paymentEntity.setUserId(checkoutSagaData.getUserId());
    paymentEntity.setAmount(checkoutSagaData.getAmount());
    paymentEntity.setCurrency(
        applicationConfigService.getConfig("currency", CommonConstant.DEFAULT_CURRENCY));
    paymentEntity.setMethod(checkoutSagaData.getPaymentMethod());
    paymentEntity.setGateway(paymentGatewayService.supports());
    paymentEntity.setStatus(PaymentStatus.PENDING);
    paymentRepository.insert(paymentEntity);

    // Create external payment
    CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
        .paymentId(paymentEntity.getId())
        .amount(paymentEntity.getAmount())
        .currency(paymentEntity.getCurrency())
        .method(paymentEntity.getMethod())
        .build();
    CreatePaymentResponse response = paymentGatewayService.createPayment(createPaymentRequest);

    if (response.isSuccess()) {
      log.info(
          "Created payment via payment gateway successfully: orderId={}, userId={}, paymentId={}, transactionId={}",
          paymentEntity.getOrderId(), paymentEntity.getUserId(), paymentEntity.getId(),
          response.getTransactionId());

      // Update payment entity
      paymentEntity.setTransactionId(response.getTransactionId());
      paymentEntity.setNextAction(response.getNextAction());
      paymentEntity.setStatus(PaymentStatus.CREATED);
      paymentRepository.update(paymentEntity);

      return SagaCommandResult.success(null);
    } else {
      log.error(
          "Failed to create payment via payment gateway: orderId={}, userId={}, paymentId={}, errorCode={}, errorMessage={}",
          paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(),
          response.getErrorCode(), response.getErrorMessage());

      // Update payment entity
      paymentEntity.setStatus(PaymentStatus.FAILED);
      paymentEntity.setErrorCode(response.getErrorCode());
      paymentEntity.setErrorMessage(StringUtil.sanitizeErrorMessage(response.getErrorMessage()));
      paymentRepository.update(paymentEntity);

      return SagaCommandResult.failure(paymentEntity.getErrorMessage());
    }
  }
}
