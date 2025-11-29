package org.atlas.domain.payment.saga.checkout;

import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.Payment;
import org.atlas.domain.payment.entity.PaymentGateway;
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
import org.atlas.framework.saga.checkout.InitializePaymentCommandMetadata;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.error.ErrorUtil;
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
    PaymentGateway paymentGateway = paymentGatewayRepository.findById(
            checkoutSagaData.getPaymentGatewayId())
        .orElseThrow(() -> {
          log.error("Payment gateway {} not found", checkoutSagaData.getPaymentGatewayId());
          return new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
        });

    // Find the corresponding payment gateway service implementation
    String paymentGatewayServiceBeanName = String.format("%sPaymentGatewayService",
        paymentGateway.getCode().toLowerCase());
    PaymentGatewayService paymentGatewayService;
    try {
      paymentGatewayService = applicationContext.getBean(
          paymentGatewayServiceBeanName, PaymentGatewayService.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
    }

    // Insert new payment entity
    Payment payment = new Payment();
    payment.setUserId(checkoutSagaData.getUser().getId());
    payment.setOrderId(checkoutSagaData.getOrderId());
    payment.setSagaId(sagaCommand.getSagaId());
    payment.setAmount(checkoutSagaData.getAmount());
    payment.setCurrency(CommonConstant.DEFAULT_CURRENCY);
    payment.setPaymentGatewayId(paymentGateway.getId());
    payment.setStatus(PaymentStatus.PENDING);
    paymentRepository.insert(payment);

    // Create external payment
    CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
        .paymentId(payment.getId())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .build();
    CreatePaymentResponse response = paymentGatewayService.createPayment(createPaymentRequest);

    if (response.isSuccess()) {
      log.info(
          "Created payment via payment gateway successfully: orderId={}, userId={}, paymentId={}, transactionId={}",
          payment.getOrderId(), payment.getUserId(), payment.getId(),
          response.getTransactionId());

      // Update payment entity
      payment.setTransactionId(response.getTransactionId());
      payment.setNextAction(response.getNextAction());
      payment.setStatus(PaymentStatus.CREATED);
      paymentRepository.update(payment);

      // Saga command result
      InitializePaymentCommandMetadata metadata = InitializePaymentCommandMetadata.builder()
          .transactionId(payment.getTransactionId())
          .paymentGatewayName(paymentGateway.getName())
          .build();
      return SagaCommandResult.success(metadata);
    } else {
      log.error(
          "Failed to create payment via payment gateway: orderId={}, userId={}, paymentId={}, errorCode={}, errorMessage={}",
          payment.getId(), payment.getUserId(), payment.getOrderId(),
          response.getErrorCode(), response.getErrorMessage());

      // Update payment entity
      payment.setStatus(PaymentStatus.FAILED);
      payment.setError(
          ErrorUtil.buildErrorMessage(response.getErrorCode(), response.getErrorMessage()));
      paymentRepository.update(payment);

      return SagaCommandResult.failure(payment.getError());
    }
  }
}
