package org.atlas.domain.payment.usecase.webhook.handler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.async.AsyncTask;
import org.atlas.framework.async.AsyncUtil;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.dependency.DependencyPort;
import org.atlas.framework.domain.event.contract.order.PaymentCanceledEvent;
import org.atlas.framework.domain.event.contract.order.PaymentFailedEvent;
import org.atlas.framework.domain.event.contract.order.PaymentSucceededEvent;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.model.PaymentResult;
import org.atlas.framework.payment.model.WebhookResponse;
import org.atlas.framework.saga.event.SagaCommandReplyEvent;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class WebhookHandler {

  private final PaymentRepository paymentRepository;
  private final ApplicationContext applicationContext;
  private final MessagePublisherPort messagePublisherPort;

  public WebhookResponse handle(PaymentGateway paymentGateway,
      Map<String, Object> payload, Map<String, String> headers) {
    log.info("Received webhook event: paymentGateway={}, payload={}, headers={}",
        paymentGateway, payload, headers);

    // Find payment gateway port implementation
    String paymentGatewayInstanceName = String.format("%sPaymentGatewayAdapter",
        paymentGateway.name().toLowerCase());
    PaymentGatewayPort paymentGatewayPort;
    try {
      paymentGatewayPort = applicationContext.getBean(
          paymentGatewayInstanceName, PaymentGatewayPort.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_SUPPORTED);
    }

    WebhookResponse response = paymentGatewayPort.handleWebhook(payload, headers);
    PaymentResult paymentResult = response.getPaymentResult();
    assert paymentResult != null;

    // Execute the remaining tasks asynchronously to be quickly respond the external payment gateway
    AsyncUtil.executeAsync(new AsyncTask() {
      @Override
      public void run() {
        // Update payment entity
        PaymentEntity paymentEntity = paymentRepository.findById(paymentResult.getPaymentId())
            .orElseThrow(() -> new DomainException(DomainError.PAYMENT_NOT_FOUND));
        switch (paymentResult.getStatus()) {
          case SUCCEEDED -> paymentEntity.setStatus(PaymentStatus.SUCCEEDED);
          case FAILED -> {
            paymentEntity.setStatus(PaymentStatus.FAILED);
            paymentEntity.setErrorCode(paymentResult.getErrorCode());
            paymentEntity.setErrorMessage(paymentResult.getErrorMessage());
          }
          case CANCELED -> {
            paymentEntity.setStatus(PaymentStatus.CANCELED);
            paymentEntity.setCancellationReason(paymentResult.getCancellationReason());
          }
        }
        paymentRepository.update(paymentEntity);

        // Publish saga command reply event
        SagaCommandReplyEvent event = SagaCommandReplyEvent.builder()
            .
            .build();
      }

      @Override
      public void onSuccess() {
        // Ignored
      }

      @Override
      public void onError(Throwable ex) {
        // Ignored
      }
    });

    return response;
  }
}
