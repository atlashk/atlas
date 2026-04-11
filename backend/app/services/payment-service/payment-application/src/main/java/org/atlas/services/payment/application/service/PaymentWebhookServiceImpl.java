package org.atlas.services.payment.application.service;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.async.AsyncUtil;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.http.HttpStatusCode;
import org.atlas.libs.framework.observability.tracing.TracingService;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.ProcessPaymentCommandMetadata;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.services.payment.domain.entity.Payment;
import org.atlas.services.payment.domain.entity.PaymentEventStatus;
import org.atlas.services.payment.domain.entity.PaymentGateway;
import org.atlas.services.payment.port.in.model.CreatePaymentEventInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentGatewayInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentEventInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;
import org.atlas.services.payment.port.in.service.PaymentEventService;
import org.atlas.services.payment.port.in.service.PaymentGatewayService;
import org.atlas.services.payment.port.in.service.PaymentService;
import org.atlas.services.payment.port.in.service.PaymentWebhookService;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookRequest;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

  private final PaymentGatewayService paymentGatewayService;
  private final PaymentEventService paymentEventService;
  private final PaymentService paymentService;
  private final SagaMessagePublisher sagaMessagePublisher;
  private final TracingService tracingService;

  @Override
  @Transactional
  public HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers) {
    log.info("Received webhook event: paymentGateway={}, rawPayload={}, headers={}",
        paymentGatewayCode, rawPayload, headers);

    // Find payment gateway
    PaymentGateway paymentGateway = paymentGatewayService.retrievePaymentGateway(
        RetrievePaymentGatewayInput.builder()
            .code(paymentGatewayCode)
            .build()
    );

    // Find the corresponding payment gateway service implementation
    PaymentGatewayIntegrationService paymentGatewayIntegrationService =
        paymentGatewayService.retrievePaymentGatewayIntegrationService(paymentGateway);

    // Create new payment event
    CreatePaymentEventInput createPaymentEventInput = CreatePaymentEventInput.builder()
        .paymentGatewayId(paymentGateway.getId())
        .payload(JsonUtil.compact(rawPayload))
        .headers(JsonUtil.toJson(headers))
        .status(PaymentEventStatus.PROCESSING)
        .build();
    Integer paymentEventId = paymentEventService.createPaymentEvent(createPaymentEventInput);

    // Delegate webhook to the payment gateway service
    HandleWebhookRequest handleRequest = HandleWebhookRequest.builder()
        .rawPayload(rawPayload)
        .headers(headers)
        .build();
    HandleWebhookResponse handleResponse = null;

    // Update payment event after handling webhook request
    UpdatePaymentEventInput updatePaymentEventInput = new UpdatePaymentEventInput();
    updatePaymentEventInput.setId(paymentEventId);
    try {
      handleResponse = paymentGatewayIntegrationService.handleWebhook(handleRequest);
      updatePaymentEventInput.setPaymentId(handleResponse.getResult().getPaymentId());
      if (handleResponse.getResponseStatus() == HttpStatusCode.OK.getCode()) {
        updatePaymentEventInput.setStatus(PaymentEventStatus.SUCCEEDED);
      } else {
        updatePaymentEventInput.setStatus(PaymentEventStatus.FAILED);
        updatePaymentEventInput.setError(
            (String) handleResponse.getResponseBody().get(HandleWebhookResponse.BODY_FIELD_ERROR));
      }
    } catch (Exception e) {
      updatePaymentEventInput.setStatus(PaymentEventStatus.FAILED);
      updatePaymentEventInput.setError(e.getMessage());
    }
    paymentEventService.updatePaymentEvent(updatePaymentEventInput);

    // Execute the remaining tasks asynchronously to be quickly respond the external payment gateway
    if (handleResponse != null && handleResponse.getResult() != null) {
      HandleWebhookResponse.Result handleResult = handleResponse.getResult();
      AsyncUtil.executeTask(() -> {
        // Retrieve payment
        final String paymentId = handleResult.getPaymentId();
        Payment payment = paymentService.retrievePayment(paymentId);

        tracingService.joinSpan(payment.getTraceId(), payment.getSpanId(),
            "saga.checkout.command.payment.webhook", () -> {
              // Update payment
              UpdatePaymentInput updatePaymentInput = new UpdatePaymentInput();
              updatePaymentInput.setId(paymentId);
              switch (handleResult.getStatus()) {
                case SUCCEEDED -> {
                  updatePaymentInput.setPaymentMethod(handleResult.getPaymentMethod());
                  updatePaymentInput.setPaymentMethodDetails(
                      JsonUtil.toJson(handleResult.getPaymentMethodDetails()));
                  updatePaymentInput.setStatus(PaymentStatus.SUCCEEDED);
                }
                case FAILED -> {
                  updatePaymentInput.setStatus(PaymentStatus.FAILED);
                  updatePaymentInput.setError(handleResult.getError());
                }
                case CANCELED -> {
                  updatePaymentInput.setStatus(PaymentStatus.CANCELED);
                  updatePaymentInput.setCancellationReason(handleResult.getCancellationReason());
                }
                default -> updatePaymentInput.setStatus(PaymentStatus.UNKNOWN);
              }
              paymentService.updatePayment(updatePaymentInput);

              // Publish saga command reply message
              SagaCommandReply.SagaCommandReplyBuilder sagaCommandReplyBuilder = SagaCommandReply.builder()
                  .sagaId(payment.getSagaId())
                  .sagaName("checkout")
                  .sagaCommandName(CheckoutCommand.PROCESS_PAYMENT);
              SagaCommandResult sagaCommandResult = null;
              switch (handleResult.getStatus()) {
                case SUCCEEDED -> sagaCommandResult = SagaCommandResult.success(
                    ProcessPaymentCommandMetadata.builder()
                        .paymentStatus(PaymentStatus.SUCCEEDED)
                        .paymentMethod(updatePaymentInput.getPaymentMethod())
                        .paymentMethodDetails(updatePaymentInput.getPaymentMethodDetails())
                        .build());
                case FAILED -> sagaCommandResult = SagaCommandResult.failure(
                    updatePaymentInput.getError(),
                    ProcessPaymentCommandMetadata.builder()
                        .paymentStatus(PaymentStatus.FAILED)
                        .build());
                case CANCELED -> sagaCommandResult = SagaCommandResult.failure(
                    updatePaymentInput.getCancellationReason(),
                    ProcessPaymentCommandMetadata.builder()
                        .paymentStatus(PaymentStatus.CANCELED)
                        .build());
              }
              sagaCommandReplyBuilder.sagaCommandResult(sagaCommandResult);
              sagaMessagePublisher.publish(sagaCommandReplyBuilder.build());
            });
      });
    }

    return handleResponse;
  }
}
