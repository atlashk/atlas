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
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.model.PaymentResult;
import org.atlas.framework.payment.model.WebhookResponse;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class WebhookHandler {

  private final PaymentRepository paymentRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final DependencyPort dependencyPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public WebhookResponse handle(PaymentGateway paymentGateway,
      Map<String, Object> payload, Map<String, String> headers) {
    log.info("Received webhook event: paymentGateway={}, payload={}, headers={}",
        paymentGateway, payload, headers);

    // Find payment gateway port implementation
    String paymentGatewayInstanceName = String.format("%sPaymentGatewayAdapter",
        paymentGateway.name().toLowerCase());
    PaymentGatewayPort paymentGatewayPort = dependencyPort.getInstanceByName(
            paymentGatewayInstanceName, PaymentGatewayPort.class)
        .orElseThrow(() -> new DomainException(AppError.PAYMENT_GATEWAY_NOT_SUPPORTED));

    WebhookResponse response = paymentGatewayPort.handleWebhook(payload, headers);

    // Execute the remaining tasks asynchronously to be quickly respond the external payment gateway
    AsyncUtil.executeAsync(new AsyncTask() {
      @Override
      public void run() {
        // Update payment entity
        PaymentResult paymentResult = new PaymentResult();
        PaymentEntity paymentEntity = paymentRepository.findById(paymentResult.getPaymentId())
            .orElseThrow(() -> new DomainException(AppError.PAYMENT_NOT_FOUND));
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

        // Publish event
        Order order = new Order();
        order.setId(paymentEntity.getOrderId());
        order.setUserId(paymentEntity.getUserId());
        order.setAmount(paymentEntity.getAmount());
        order.setPaymentId(paymentEntity.getId());
        order.setPaymentMethod(paymentEntity.getMethod());
        switch (paymentResult.getStatus()) {
          case SUCCEEDED -> {
            PaymentSucceededEvent paymentSucceededEvent = new PaymentSucceededEvent(
                applicationConfigPort.getApplicationName(), order);
            externalMessagePublisherPort.publish(paymentSucceededEvent);
          }
          case FAILED -> {
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
                applicationConfigPort.getApplicationName(), order);
            paymentFailedEvent.setErrorCode(paymentResult.getErrorCode());
            paymentFailedEvent.setErrorMessage(paymentResult.getErrorMessage());
            externalMessagePublisherPort.publish(paymentFailedEvent);
          }
          case CANCELED -> {
            PaymentCanceledEvent paymentCanceledEvent = new PaymentCanceledEvent(
                applicationConfigPort.getApplicationName(), order);
            paymentCanceledEvent.setCancellationReason(paymentResult.getCancellationReason());
            externalMessagePublisherPort.publish(paymentCanceledEvent);
          }
        }
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
