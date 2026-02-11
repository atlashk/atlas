package org.atlas.services.payment.application.front.saga.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.payment.PaymentStatus;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.ExceptionUtil;
import org.atlas.libs.framework.json.jackson.JacksonService;
import org.atlas.libs.framework.payment.PaymentGatewayService;
import org.atlas.libs.framework.payment.model.CreatePaymentRequest;
import org.atlas.libs.framework.payment.model.CreatePaymentResponse;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.checkout.InitializePaymentCommandMetadata;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;
import org.atlas.services.payment.port.out.repository.PaymentGatewayRepository;
import org.atlas.services.payment.port.out.repository.PaymentRepository;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializePaymentCommandHandler {

  private final ApplicationContext applicationContext;
  private final PaymentRepository paymentRepository;
  private final PaymentGatewayRepository paymentGatewayRepository;
  private final SequenceGenerator sequenceGenerator;

  @SagaCommandHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public SagaCommandResult initializePayment(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
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
        paymentGateway.getCode().toLowerCase());
    PaymentGatewayService paymentGatewayService;
    try {
      paymentGatewayService = applicationContext.getBean(
          paymentGatewayServiceBeanName, PaymentGatewayService.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
    }

    // Insert new payment entity
    PaymentEntity payment = new PaymentEntity();
    payment.setId(sequenceGenerator.generate(SequenceType.PAYMENT));
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
          payment.getOrderId(), payment.getUserId(), payment.getId(),
          response.getErrorCode(), response.getErrorMessage());

      // Update payment entity
      payment.setStatus(PaymentStatus.FAILED);
      payment.setError(
          ExceptionUtil.buildErrorMessage(response.getErrorCode(), response.getErrorMessage()));
      paymentRepository.update(payment);

      return SagaCommandResult.failure(payment.getError());
    }
  }
}
