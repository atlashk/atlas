package org.atlas.domain.payment.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.service.PaymentRoutingService;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.PaymentCreatedEvent;
import org.atlas.framework.domain.event.contract.order.PaymentFailedEvent;
import org.atlas.framework.domain.event.contract.order.ProductReservationSucceededEvent;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVATION_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class ProductReservationSucceededEventHandler {

  private final PaymentRepository paymentRepository;
  private final PaymentRoutingService paymentRoutingService;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public void handle(ProductReservationSucceededEvent productReservationSucceededEvent) {
    final Order order = productReservationSucceededEvent.getOrder();

    try {
      // Find the relevant payment gateway
      PaymentGatewayPort paymentGatewayPort = paymentRoutingService.getPaymentGateway(
          order.getPaymentMethod());

      // Insert new payment entity
      PaymentEntity paymentEntity = new PaymentEntity();
      paymentEntity.setOrderId(order.getId());
      paymentEntity.setUserId(order.getUserId());
      paymentEntity.setAmount(order.getAmount());
      paymentEntity.setCurrency(applicationConfigPort.getConfig(
          Application.PAYMENT_SERVICE, "currency", CommonConstant.DEFAULT_CURRENCY));
      paymentEntity.setMethod(order.getPaymentMethod());
      paymentEntity.setGateway(paymentGatewayPort.supports());
      paymentRepository.insert(paymentEntity);
      order.setPaymentId(paymentEntity.getId());

      // Create external payment
      CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
          .paymentId(paymentEntity.getId())
          .amount(paymentEntity.getAmount())
          .currency(paymentEntity.getCurrency())
          .method(paymentEntity.getMethod())
          .build();
      CreatePaymentResponse response = paymentGatewayPort.createPayment(createPaymentRequest);

      if (response.isSuccess()) {
        log.info(
            "Created payment via payment gateway successfully: orderId={}, userId={}, paymentId={}, transactionId={}",
            paymentEntity.getOrderId(), paymentEntity.getUserId(), paymentEntity.getId(),
            response.getTransactionId());

        // Update payment entity
        paymentEntity.setTransactionId(response.getTransactionId());
        paymentEntity.setStatus(PaymentStatus.CREATED);
        paymentRepository.update(paymentEntity);

        // Publish PAYMENT_CREATED event
        PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(
            applicationConfigPort.getApplicationName(), order);
        paymentCreatedEvent.setNextAction(response.getNextAction());
        externalMessagePublisherPort.publish(paymentCreatedEvent);
      } else {
        log.error(
            "Failed to create payment via payment gateway: orderId={}, userId={}, paymentId={}, errorCode={}, errorMessage={}",
            paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(),
            response.getErrorCode(), response.getErrorMessage());

        // Update payment entity
        paymentEntity.setStatus(PaymentStatus.FAILED);
        paymentEntity.setErrorCode(response.getErrorCode());
        paymentEntity.setErrorMessage(response.getErrorMessage());
        paymentRepository.update(paymentEntity);

        // Publish PAYMENT_FAILED event
        PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
            applicationConfigPort.getApplicationName(), order);
        paymentFailedEvent.setErrorCode(response.getErrorCode());
        paymentFailedEvent.setErrorMessage(response.getErrorMessage());
        externalMessagePublisherPort.publish(paymentFailedEvent);
      }
    } catch (Exception e) {
      log.error("Error create payment: orderId={}, userId={}, error={}",
          order.getUserId(), order.getId(), e.getMessage(), e);

      // Publish PAYMENT_FAILED event
      PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
          applicationConfigPort.getApplicationName(), order);
      paymentFailedEvent.setErrorMessage(e.getMessage());
      externalMessagePublisherPort.publish(paymentFailedEvent);
    }
  }
}
