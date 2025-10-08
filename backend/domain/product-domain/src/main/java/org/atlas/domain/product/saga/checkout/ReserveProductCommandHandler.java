package org.atlas.domain.product.saga.checkout;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.entity.ReservationEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.ReservationRepository;
import org.atlas.domain.product.shared.DecreaseQuantityStrategy;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.saga.annotation.SagaCompensationHandler;
import org.atlas.framework.saga.annotation.SagaCommandHandler;
import org.atlas.framework.saga.command.CheckoutCommand;
import org.atlas.framework.saga.context.CheckoutSagaData;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveProductCommandHandler {

  private final ProductRepository productRepository;
  private final ReservationRepository reservationRepository;
  private final ApplicationConfigService applicationConfigService;
  private final LockPort lockPort;

  @SagaCommandHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public void reserveProduct(SagaCommand event) {
    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    CheckoutSagaData checkoutSagaData = sagaContext.get("data", CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Try to reserve products
    checkoutSagaData.getOrderItems()
        .forEach(orderItem -> {
          decreaseQuantity(orderItem.getProductId(), orderItem.getQuantity());

          // Insert new reservation
          ReservationEntity reservation = ReservationEntity.builder()
              .orderId(checkoutSagaData.getOrderId())
              .productId(orderItem.getProductId())
              .quantity(orderItem.getQuantity())
              .build();
          reservationRepository.insert(reservation);
        });
    log.info("Successfully reserved products: sagaId={}, orderId={}",
        sagaContext.getSagaId(), checkoutSagaData.getOrderId());
  }

  private void decreaseQuantity(Integer productId, Integer quantity) {
    DecreaseQuantityStrategy decreaseQuantityStrategy =
        applicationConfigService.getConfigAsClass("decrease-quantity-strategy",
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
        try {
          boolean acquiredLock = lockPort.acquireLock(lockKey, waitTime, leaseTime);
          if (!acquiredLock) {
            throw new LockAcquisitionException("Failed to acquire lock: " + lockKey);
          }
          ProductEntity productEntity = productRepository.findById(productId)
              .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
          if (productEntity.getQuantity() < quantity) {
            throw new DomainException(DomainError.PRODUCT_INSUFFICIENT_QUANTITY);
          }
          productEntity.setQuantity(productEntity.getQuantity() - quantity);
          productRepository.update(productEntity);
        } finally {
          lockPort.releaseLock(lockKey);
        }
      }
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + decreaseQuantityStrategy);
    }
  }

  @SagaCompensationHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public void compensateReserveProduct(SagaCompensation event) {
    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    CheckoutSagaData checkoutSagaData = sagaContext.get("data", CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    checkoutSagaData.getOrderItems()
        .forEach(orderItem -> {
          // Check reservation exists or not
          ReservationEntity reservation = reservationRepository.findByOrderIdAndProductId(
                  checkoutSagaData.getOrderId(), orderItem.getProductId())
              .orElseThrow(() -> new DomainException(DomainError.RESERVATION_NOT_FOUND));

          // Increase quantity to compensate
          productRepository.increaseQuantity(orderItem.getProductId(), orderItem.getQuantity());

          // Delete reservation
          reservationRepository.delete(reservation);
        });
  }
}
