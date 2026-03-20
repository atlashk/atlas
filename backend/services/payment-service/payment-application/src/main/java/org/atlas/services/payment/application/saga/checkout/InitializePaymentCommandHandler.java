package org.atlas.services.payment.application.saga.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.observability.tracing.TracingService;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.checkout.InitializePaymentCommandMetadata;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.util.ExceptionUtil;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;
import org.atlas.services.payment.port.in.model.CreatePaymentInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentGatewayInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;
import org.atlas.services.payment.port.in.service.PaymentGatewayService;
import org.atlas.services.payment.port.in.service.PaymentService;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentRequest;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentResponse;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializePaymentCommandHandler {

  private final PaymentGatewayService paymentGatewayService;
  private final PaymentService paymentService;
  private final TracingService tracingService;

  @SagaCommandHandler(command = CheckoutCommand.INITIALIZE_PAYMENT)
  public SagaCommandResult initializePayment(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.JSON_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Find payment gateway
    PaymentGatewayEntity paymentGateway = paymentGatewayService.retrievePaymentGateway(
        RetrievePaymentGatewayInput.builder()
            .id(checkoutSagaData.getPaymentGatewayId())
            .build()
    );

    // Find the corresponding payment gateway integration service implementation
    PaymentGatewayIntegrationService paymentGatewayIntegrationService =
        paymentGatewayService.retrievePaymentGatewayIntegrationService(paymentGateway);

    // Create new payment entity
    final String userId = checkoutSagaData.getUser().getId();
    final String orderId = checkoutSagaData.getOrderId();
    final String currentTraceId = tracingService.getCurrentTraceId();
    final String currentSpanId = tracingService.getCurrentSpanId();
    CreatePaymentInput createPaymentInput = CreatePaymentInput.builder()
        .userId(userId)
        .orderId(orderId)
        .sagaId(sagaCommand.getSagaId())
        .amount(checkoutSagaData.getAmount())
        .currency(CommonConstant.DEFAULT_CURRENCY)
        .paymentGatewayId(paymentGateway.getId())
        .status(PaymentStatus.PENDING)
        .traceId(StringUtil.defaultIfBlank(currentTraceId, null))
        .spanId(StringUtil.defaultIfBlank(currentSpanId, null))
        .build();
    String paymentId = paymentService.createPayment(createPaymentInput);

    // Create external payment
    CreateExternalPaymentRequest createExternalPaymentRequest = CreateExternalPaymentRequest.builder()
        .paymentId(paymentId)
        .amount(createPaymentInput.getAmount())
        .currency(createPaymentInput.getCurrency())
        .build();
    CreateExternalPaymentResponse response = paymentGatewayIntegrationService.createPayment(
        createExternalPaymentRequest);

    // Proceed the response of external payment creation
    if (response.isSuccess()) {
      log.info(
          "Created payment successfully: "
              + "orderId={}, userId={}, paymentId={}, paymentGateway={}, transactionId={}",
          orderId, userId, paymentId, paymentGateway.getCode(), response.getTransactionId());

      // Update payment status
      UpdatePaymentInput updatePaymentInput = UpdatePaymentInput.builder()
          .id(paymentId)
          .status(PaymentStatus.CREATED)
          .transactionId(response.getTransactionId())
          .nextAction(response.getNextAction())
          .build();
      paymentService.updatePayment(updatePaymentInput);

      // Saga command result
      InitializePaymentCommandMetadata metadata = InitializePaymentCommandMetadata.builder()
          .transactionId(response.getTransactionId())
          .paymentGatewayName(paymentGateway.getName())
          .build();
      return SagaCommandResult.success(metadata);
    } else {
      log.error(
          "Failed to create payment via payment gateway: "
              + "orderId={}, userId={}, paymentId={}, paymentGateway={}, errorCode={}, errorMessage={}",
          orderId, userId, paymentId, paymentGateway.getCode(), response.getErrorCode(), response.getErrorMessage());

      // Update payment status
      String error = ExceptionUtil.buildErrorMessage(
          response.getErrorCode(), response.getErrorMessage());
      UpdatePaymentInput updatePaymentInput = UpdatePaymentInput.builder()
          .id(paymentId)
          .status(PaymentStatus.FAILED)
          .error(error)
          .build();
      paymentService.updatePayment(updatePaymentInput);

      // Saga command result
      return SagaCommandResult.failure(error);
    }
  }
}
