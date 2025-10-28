package org.atlas.domain.payment.saga.checkout;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.entity.PaymentGatewayEntity;
import org.atlas.domain.payment.repository.PaymentGatewayRepository;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.payment.PaymentGatewayService;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.util.ErrorUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializePaymentCommandHandler {

  private final PaymentRepository paymentRepository;
  private final PaymentGatewayRepository paymentGatewayRepository;
  private final ApplicationContext applicationContext;

  @SagaCommandHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public SagaCommandResult initializePayment(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Find payment gateway
    PaymentGatewayEntity paymentGateway = paymentGatewayRepository.findById(
            checkoutSagaData.getPaymentGatewayId())
        .orElseThrow(() -> {
          log.error("Payment gateway {} not found", checkoutSagaData.getPaymentGatewayId());
          return new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
        });

    // Find the corresponding payment gateway service implementation
    String paymentGatewayServiceBeanName = String.format("%sPaymentGatewayService",
        paymentGateway.getCode().toUpperCase());
    PaymentGatewayService paymentGatewayService;
    try {
      paymentGatewayService = applicationContext.getBean(
          paymentGatewayServiceBeanName, PaymentGatewayService.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
    }

    // Insert new payment entity
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setUserId(checkoutSagaData.getUserId());
    paymentEntity.setOrderId(checkoutSagaData.getOrderId());
    paymentEntity.setSagaId(sagaCommand.getSagaId());
    paymentEntity.setAmount(checkoutSagaData.getAmount());
    paymentEntity.setCurrency(CommonConstant.DEFAULT_CURRENCY);
    paymentEntity.setPaymentGatewayId(paymentGateway.getId());
    paymentEntity.setStatus(PaymentStatus.PENDING);
    paymentRepository.insert(paymentEntity);

    // Create external payment
    CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
        .paymentId(paymentEntity.getId())
        .amount(paymentEntity.getAmount())
        .currency(paymentEntity.getCurrency())
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
      paymentEntity.setError(
          ErrorUtil.buildErrorMessage(response.getErrorCode(), response.getErrorMessage()));
      paymentRepository.update(paymentEntity);

      return SagaCommandResult.failure(paymentEntity.getError());
    }
  }
}
