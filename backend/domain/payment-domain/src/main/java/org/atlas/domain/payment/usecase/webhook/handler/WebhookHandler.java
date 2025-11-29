package org.atlas.domain.payment.usecase.webhook.handler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.Payment;
import org.atlas.domain.payment.entity.PaymentEvent;
import org.atlas.domain.payment.entity.PaymentEventStatus;
import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.domain.payment.repository.PaymentEventRepository;
import org.atlas.domain.payment.repository.PaymentGatewayRepository;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.http.HttpStatusCode;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.payment.PaymentGatewayService;
import org.atlas.framework.payment.model.HandleWebhookRequest;
import org.atlas.framework.payment.model.HandleWebhookResponse;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.ProcessPaymentCommandMetadata;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.framework.concurrent.AsyncUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class WebhookHandler {

  private final PaymentGatewayRepository paymentGatewayRepository;
  private final PaymentEventRepository paymentEventRepository;
  private final PaymentRepository paymentRepository;
  private final ApplicationContext applicationContext;
  private final SagaMessagePublisher sagaMessagePublisher;

  public HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers) {
    log.info("Received webhook event: paymentGateway={}, rawPayload={}, headers={}",
        paymentGatewayCode, rawPayload, headers);

    // Find payment gateway
    PaymentGateway paymentGateway = paymentGatewayRepository.findByCode(
            paymentGatewayCode.toUpperCase())
        .orElseThrow(() -> {
          log.error("Payment gateway {} not found", paymentGatewayCode);
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

    // Persist payment event
    PaymentEvent paymentEvent = PaymentEvent.builder()
        .paymentGatewayId(paymentGateway.getId())
        .payload(JsonUtil.getInstance().compact(rawPayload))
        .headers(JsonUtil.getInstance().toJson(headers))
        .status(PaymentEventStatus.PROCESSING)
        .build();
    paymentEventRepository.insert(paymentEvent);

    // Delegate webhook to the payment gateway service
    HandleWebhookRequest handleRequest = HandleWebhookRequest.builder()
        .rawPayload(rawPayload)
        .headers(headers)
        .build();
    HandleWebhookResponse handleResponse = null;

    // Update payment event status
    try {
      handleResponse = paymentGatewayService.handleWebhook(handleRequest);
      paymentEvent.setPaymentId(handleResponse.getResult().getPaymentId());
      if (handleResponse.getResponseStatus() == HttpStatusCode.OK.getCode()) {
        paymentEvent.setStatus(PaymentEventStatus.SUCCEEDED);
      } else {
        paymentEvent.setStatus(PaymentEventStatus.FAILED);
        paymentEvent.setError(
            (String) handleResponse.getResponseBody().get(HandleWebhookResponse.BODY_FIELD_ERROR));
      }
      paymentEventRepository.update(paymentEvent);
    } catch (Exception e) {
      paymentEvent.setStatus(PaymentEventStatus.FAILED);
      paymentEvent.setError(e.getMessage());
      paymentEventRepository.update(paymentEvent);
    }

    // Execute the remaining tasks asynchronously to be quickly respond the external payment gateway
    if (handleResponse != null && handleResponse.getResult() != null) {
      HandleWebhookResponse.Result handleResult = handleResponse.getResult();
      AsyncUtil.executeTask(() -> {
        // Update payment entity
        Payment payment = paymentRepository.findById(handleResult.getPaymentId())
            .orElseThrow(() -> new DomainException(DomainError.PAYMENT_NOT_FOUND));
        switch (handleResult.getStatus()) {
          case SUCCEEDED -> {
            payment.setPaymentMethod(handleResult.getPaymentMethod());
            payment.setPaymentMethodDetails(
                JsonUtil.getInstance().toJson(handleResult.getPaymentMethodDetails()));
            payment.setStatus(PaymentStatus.SUCCEEDED);
          }
          case FAILED -> {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setError(handleResult.getError());
          }
          case CANCELED -> {
            payment.setStatus(PaymentStatus.CANCELED);
            payment.setCancellationReason(handleResult.getCancellationReason());
          }
          default -> payment.setStatus(PaymentStatus.UNKNOWN);
        }
        paymentRepository.update(payment);

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
                  .paymentMethod(payment.getPaymentMethod())
                  .paymentMethodDetails(payment.getPaymentMethodDetails())
                  .build());
          case FAILED -> sagaCommandResult = SagaCommandResult.failure(
              payment.getError(),
              ProcessPaymentCommandMetadata.builder()
                  .paymentStatus(PaymentStatus.FAILED)
                  .build());
          case CANCELED -> sagaCommandResult = SagaCommandResult.failure(
              payment.getCancellationReason(),
              ProcessPaymentCommandMetadata.builder()
                  .paymentStatus(PaymentStatus.CANCELED)
                  .build());
        }
        sagaCommandReplyBuilder.sagaCommandResult(sagaCommandResult);
        sagaMessagePublisher.publish(sagaCommandReplyBuilder.build());
      });
    }

    return handleResponse;
  }
}
