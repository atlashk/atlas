package org.atlas.domain.product.event;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.shared.enums.DecreaseQuantityStrategy;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.event.contract.order.ReserveQuantityFailedEvent;
import org.atlas.framework.domain.event.contract.order.ReserveQuantitySucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.messaging.MessagePublisherPort;

@DomainEventHandler(type = DomainEventType.ORDER_CREATED)
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventHandler {

  private final ProductRepository productRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final LockPort lockPort;
  private final MessagePublisherPort messagePublisherPort;

  public void handle(OrderCreatedEvent orderCreatedEvent) {
    try {
      orderCreatedEvent.getOrderItems().forEach(orderItem ->
          decreaseQuantity(orderItem.getProduct().getId(), orderItem.getQuantity()));
      ReserveQuantitySucceededEvent reserveQuantitySucceededEvent =
          new ReserveQuantitySucceededEvent(applicationConfigPort.getApplicationName());
      reserveQuantitySucceededEvent.merge(orderCreatedEvent);
      messagePublisherPort.publish(reserveQuantitySucceededEvent);
    } catch (Exception e) {
      log.error("Failed to handle event {}", orderCreatedEvent.getEventId(), e);
      ReserveQuantityFailedEvent reserveQuantityFailedEvent =
          new ReserveQuantityFailedEvent(applicationConfigPort.getApplicationName());
      reserveQuantityFailedEvent.merge(orderCreatedEvent);
      reserveQuantityFailedEvent.setError(e.getMessage());
      messagePublisherPort.publish(reserveQuantityFailedEvent);
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
        lockPort.doWithLock(() -> {
          ProductEntity productEntity = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(AppError.PRODUCT_NOT_FOUND));
          if (productEntity.getQuantity() < quantity) {
            throw new DomainException(AppError.PRODUCT_INSUFFICIENT_QUANTITY);
          }
          productEntity.setQuantity(productEntity.getQuantity() - quantity);
          productRepository.update(productEntity);
        }, lockKey, Duration.ofSeconds(5));
      }
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + decreaseQuantityStrategy);
    }
  }
}
