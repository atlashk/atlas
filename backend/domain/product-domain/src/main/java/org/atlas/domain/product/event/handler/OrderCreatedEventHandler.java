package org.atlas.domain.product.event.handler;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.shared.DecreaseQuantityStrategy;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.event.contract.product.ProductReservationFailedEvent;
import org.atlas.framework.domain.event.contract.product.ProductReservationSucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.ORDER_CREATED)
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventHandler {

  private final ProductRepository productRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final LockPort lockPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public void handle(OrderCreatedEvent orderCreatedEvent) {
    try {
      // Try to reserve products
      orderCreatedEvent.getOrder().getOrderItems().forEach(orderItem ->
          decreaseQuantity(orderItem.getProduct().getId(), orderItem.getQuantity()));
      log.info("Successfully reserved products: eventId={}, orderId={}",
          orderCreatedEvent.getEventId(), orderCreatedEvent.getOrder().getOrderId());

      // Publish succeeded event
      ProductReservationSucceededEvent productReservationSucceededEvent =
          new ProductReservationSucceededEvent(applicationConfigPort.getApplicationName());
      productReservationSucceededEvent.setOrder(orderCreatedEvent.getOrder());
      externalMessagePublisherPort.publish(productReservationSucceededEvent);
    } catch (Exception e) {
      log.error("Failed to reserve products: eventId={}, orderId={}, error={}",
          orderCreatedEvent.getEventId(), orderCreatedEvent.getOrder().getOrderId(), e.getMessage(),
          e);

      // Publish failed event
      ProductReservationFailedEvent productReservationFailedEvent =
          new ProductReservationFailedEvent(applicationConfigPort.getApplicationName());
      productReservationFailedEvent.setOrder(orderCreatedEvent.getOrder());
      productReservationFailedEvent.setError(e.getMessage());
      externalMessagePublisherPort.publish(productReservationFailedEvent);
    }
  }

  private void decreaseQuantity(Integer productId, Integer quantity) {
    DecreaseQuantityStrategy decreaseQuantityStrategy =
        applicationConfigPort.getConfigAsClass(Application.PRODUCT_SERVICE,
            "decrease-quantity-strategy",
            DecreaseQuantityStrategy.class, DecreaseQuantityStrategy.CONSTRAINT);
    switch (decreaseQuantityStrategy) {
      case CONSTRAINT -> productRepository.decreaseQuantityWithConstraint(productId, quantity);
      case PESSIMISTIC_LOCK ->
          productRepository.decreaseQuantityWithPessimisticLock(productId, quantity);
      case OPTIMISTIC_LOCK ->
          productRepository.decreaseQuantityWithOptimisticLock(productId, quantity);
      case DISTRIBUTED_LOCK -> {
        final String lockKey = String.format("product:%d:decrease-quantity", productId);
        final Duration waitTime = Duration.ofSeconds(5);
        final Duration leaseTime = Duration.ofSeconds(15);
        lockPort.doWithLock(() -> {
          ProductEntity productEntity = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));
          if (productEntity.getQuantity() < quantity) {
            throw new DomainException(AppError.PRODUCT_INSUFFICIENT_QUANTITY);
          }
          productEntity.setQuantity(productEntity.getQuantity() - quantity);
          productRepository.update(productEntity);
        }, lockKey, waitTime, leaseTime, true);
      }
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + decreaseQuantityStrategy);
    }
  }
}
