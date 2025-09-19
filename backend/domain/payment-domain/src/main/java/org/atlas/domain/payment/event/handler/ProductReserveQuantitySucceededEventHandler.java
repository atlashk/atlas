package org.atlas.domain.payment.event.handler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.enums.PaymentStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.payment.PaymentCreatedEvent;
import org.atlas.framework.domain.event.contract.product.ProductReserveQuantitySucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.payment.PaymentGatewayService;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED)
@RequiredArgsConstructor
@Slf4j
public class ProductReserveQuantitySucceededEventHandler {

  private final PaymentRepository paymentRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;
  private final PaymentGatewayService paymentGatewayService;

  public void handle(ProductReserveQuantitySucceededEvent event) {
    // Find payment gateway
    PaymentGatewayPort paymentGatewayPort = paymentGatewayService.getPaymentGatewayPort(event.getOrder().getPaymentGateway())
        .orElseThrow(() -> new DomainException(AppError.PAYMENT_GATEWAY_NOT_SUPPORTED));

    // Create payment entity
    PaymentEntity paymentEntity = newPaymentEntity(event);
    paymentRepository.save(paymentEntity);

    Map<String, Object> paymentData = paymentGatewayPort.createPayment(
        paymentEntity.getOrderId(), paymentEntity.getUserId(), paymentEntity.getAmount(),
        paymentEntity.getCurrency());
    if ((boolean) paymentData.get("result")) {
      final String transactionId = paymentData.getOrDefault("transactionId", "").toString();
      final String receiptUrl = paymentData.getOrDefault("receiptUrl", "").toString();

      log.info(
          "Successfully created payment: paymentId={}, userId={}, orderId={}, transactionId={}, receiptUrl={}",
          paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(),
          transactionId, receiptUrl);

      // Update payment entity
      paymentEntity.setTransactionId(transactionId);
      paymentEntity.setReceiptUrl(receiptUrl);
      paymentEntity.setStatus(PaymentStatus.CREATED);
      paymentRepository.save(paymentEntity);

      // Publish PAYMENT_CREATED event
      PaymentCreatedEvent paymentCreatedEvent = newPaymentCreatedEvent(paymentEntity, paymentData);
      externalMessagePublisherPort.publish(paymentCreatedEvent);
    } else {
      final String errorCode = paymentData.getOrDefault("errorCode", "").toString();
      final String errorMessage = paymentData.getOrDefault("errorMessage", "").toString();
      log.error(
          "Failed to create payment: paymentId={}, userId={}, orderId={}, errorCode={}, errorMessage={}",
          paymentEntity.getId(), paymentEntity.getUserId(), paymentEntity.getOrderId(), errorCode,
          errorMessage);

      // Update payment entity
      paymentEntity.setStatus(PaymentStatus.FAILED);
      paymentEntity.setErrorCode(errorCode);
      paymentEntity.setErrorMessage(errorMessage);
      paymentRepository.save(paymentEntity);
    }
  }

  private PaymentEntity newPaymentEntity(ProductReserveQuantitySucceededEvent event) {
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.setOrderId(event.getOrder().getOrderId());
    paymentEntity.setUserId(event.getOrder().getUser().getId());
    paymentEntity.setAmount(event.getOrder().getAmount());
    paymentEntity.setCurrency(applicationConfigPort.getConfig(
        Application.PAYMENT_SERVICE, "currency", "USD"));
    return paymentEntity;
  }

  private PaymentCreatedEvent newPaymentCreatedEvent(PaymentEntity paymentEntity,
      Map<String, Object> paymentGatewayResponse) {
    PaymentCreatedEvent event = new PaymentCreatedEvent(applicationConfigPort.getApplicationName(),
        paymentEntity.getId(), paymentEntity.getOrderId());
    event.setPaymentData(paymentGatewayResponse);
    return event;
  }
}
