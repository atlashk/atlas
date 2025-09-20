package org.atlas.domain.payment.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.dependency.DependencyPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.payment.PaymentCreatedEvent;
import org.atlas.framework.domain.event.contract.payment.PaymentFailedEvent;
import org.atlas.framework.domain.event.contract.product.ProductReservationSucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVATION_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class ProductReservationSucceededEventHandler {

  private final PaymentRepository paymentRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final DependencyPort dependencyPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public void handle(ProductReservationSucceededEvent event) {
    // Find payment gateway
    PaymentGateway paymentGateway = applicationConfigPort.getConfigAsClass(
        Application.PAYMENT_SERVICE, "defaultGateway",
        PaymentGateway.class, PaymentGateway.STRIPE);

    // Find payment gateway port implementation
    String paymentGatewayInstanceName = String.format("%sPaymentGatewayAdapter",
        paymentGateway.name().toLowerCase());
    PaymentGatewayPort paymentGatewayPort = dependencyPort.getInstanceByName(
            paymentGatewayInstanceName, PaymentGatewayPort.class)
        .orElseThrow(() -> new DomainException(AppError.PAYMENT_GATEWAY_NOT_SUPPORTED));

    // Create payment entity
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setOrderId(event.getOrder().getOrderId());
    paymentEntity.setUserId(event.getOrder().getUser().getId());
    paymentEntity.setAmount(event.getOrder().getAmount());
    paymentEntity.setCurrency(applicationConfigPort.getConfig(
        Application.PAYMENT_SERVICE, "currency", "USD"));
    paymentEntity.setMethod(event.getOrder().getPaymentMethod());
    paymentEntity.setGateway(paymentGateway);
    paymentRepository.save(paymentEntity);

    // Create external payment
    CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
        .paymentId(paymentEntity.getId())
        .amount(paymentEntity.getAmount())
        .currency(paymentEntity.getCurrency())
        .method(event.getOrder().getPaymentMethod())
        .build();
    CreatePaymentResponse response = paymentGatewayPort.createPayment(createPaymentRequest);

    if (response.isSuccess()) {
      final String transactionId = response.getData().getOrDefault("transactionId", "")
          .toString();
      final String receiptUrl = response.getData().getOrDefault("receiptUrl", "").toString();
      log.info(
          "Created payment successfully: paymentId={}, userId={}, orderId={}, transactionId={}, receiptUrl={}",
          paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(),
          transactionId, receiptUrl);

      // Update payment entity
      paymentEntity.setTransactionId(transactionId);
      paymentEntity.setReceiptUrl(receiptUrl);
      paymentEntity.setStatus(PaymentStatus.CREATED);
      paymentRepository.save(paymentEntity);

      // Publish PAYMENT_CREATED event
      PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(
          applicationConfigPort.getApplicationName());
      paymentCreatedEvent.setOrderId(paymentEntity.getOrderId());
      response.getData().put("gateway", paymentGateway);
      paymentCreatedEvent.setPaymentData(response.getData());
      externalMessagePublisherPort.publish(paymentCreatedEvent);
    } else {
      log.error(
          "Failed to create payment: paymentId={}, userId={}, orderId={}, errorCode={}, errorMessage={}",
          paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(),
          response.getErrorCode(), response.getErrorMessage());

      // Update payment entity
      paymentEntity.setStatus(PaymentStatus.FAILED);
      paymentEntity.setErrorCode(response.getErrorCode());
      paymentEntity.setErrorMessage(response.getErrorMessage());
      paymentRepository.save(paymentEntity);

      // Publish PAYMENT_FAILED event
      PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
          applicationConfigPort.getApplicationName());
      paymentFailedEvent.setOrderId(paymentEntity.getOrderId());
      paymentFailedEvent.setErrorCode(response.getErrorCode());
      paymentFailedEvent.setErrorMessage(response.getErrorMessage());
      externalMessagePublisherPort.publish(paymentFailedEvent);
    }
  }
}
