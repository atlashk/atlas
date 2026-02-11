package org.atlas.services.product.application.front.saga.checkout;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.common.exception.OutOfStockException;
import org.atlas.libs.framework.domain.product.DecreaseQuantityStrategy;
import org.atlas.libs.framework.json.jackson.JacksonService;
import org.atlas.libs.framework.lock.LockAcquisitionException;
import org.atlas.libs.framework.lock.LockService;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.libs.framework.saga.core.annotation.SagaCompensationHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.compensation.SagaCompensationResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.port.out.repository.ReservationRepository;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.domain.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveProductCommandHandler {

  private final ProductRepository productRepository;
  private final ReservationRepository reservationRepository;
  private final ApplicationConfigService applicationConfigService;
  private final LockService lockService;

  @SagaCommandHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public SagaCommandResult reserveProduct(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Try to reserve products
    for (CheckoutSagaData.OrderItem orderItem : checkoutSagaData.getOrderItems()) {
      try {
        decreaseQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
      } catch (OutOfStockException e) {
        log.error("Out of stock occurred for product {}: {}",
            orderItem.getProduct().getId(), e.getMessage(), e);
        return SagaCommandResult.failure(
            String.format("Product %s is out of stock", orderItem.getProduct().getName()));
      } catch (Exception e) {
        log.error("Failed to reserve product {}: {}", orderItem.getProduct().getId(),
            e.getMessage(), e);
        return SagaCommandResult.failure(String.format("Something went wrong with product %s",
            orderItem.getProduct().getName()));
      }

      // Insert new reservation
      ReservationEntity reservation = ReservationEntity.builder()
          .orderId(checkoutSagaData.getOrderId())
          .productId(orderItem.getProduct().getId())
          .quantity(orderItem.getQuantity())
          .build();
      reservationRepository.insert(reservation);
    }

    log.info("Successfully reserved products: sagaId={}, orderId={}",
        sagaCommand.getSagaId(), checkoutSagaData.getOrderId());
    return SagaCommandResult.success();
  }

  private void decreaseQuantity(String productId, Integer quantity)
      throws OutOfStockException {
    DecreaseQuantityStrategy decreaseQuantityStrategy =
        applicationConfigService.getConfigAsClass("product.decrease-quantity-strategy",
            DecreaseQuantityStrategy.class, DecreaseQuantityStrategy.CONSTRAINT);
    switch (decreaseQuantityStrategy) {
      case CONSTRAINT -> decreaseQuantityWithConstraint(productId, quantity);
      case PESSIMISTIC_LOCK -> decreaseQuantityWithPessimisticLock(productId, quantity);
      case OPTIMISTIC_LOCK -> decreaseQuantityWithOptimisticLock(productId, quantity);
      case DISTRIBUTED_LOCK -> decreaseQuantityWithDistributedLock(productId, quantity);
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + decreaseQuantityStrategy);
    }
  }

  private void decreaseQuantityWithConstraint(String productId, Integer quantity)
      throws OutOfStockException {
    productRepository.decreaseQuantityWithConstraint(productId, quantity);
  }

  private void decreaseQuantityWithPessimisticLock(String productId, Integer quantity)
      throws OutOfStockException {
    productRepository.decreaseQuantityWithPessimisticLock(productId, quantity);
  }

  private void decreaseQuantityWithOptimisticLock(String productId, Integer quantity)
      throws OutOfStockException {
    productRepository.decreaseQuantityWithOptimisticLock(productId, quantity);
  }

  private void decreaseQuantityWithDistributedLock(String productId, Integer quantity)
      throws OutOfStockException {
    final String lockKey = String.format("product:%s:decrease-quantity", productId);
    final Duration waitTime = Duration.ofSeconds(5);
    final Duration leaseTime = Duration.ofSeconds(15);
    try {
      boolean acquiredLock = lockService.acquireLock(lockKey, waitTime, leaseTime);
      if (!acquiredLock) {
        throw new LockAcquisitionException("Failed to acquire lock: " + lockKey);
      }
      ProductEntity product = productRepository.findById(productId)
          .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
      if (product.getQuantity() < quantity) {
        throw new OutOfStockException();
      }
      product.setQuantity(product.getQuantity() - quantity);
      productRepository.update(product);
    } finally {
      lockService.releaseLock(lockKey);
    }
  }

  @SagaCompensationHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public SagaCompensationResult compensateReserveProduct(SagaCompensation sagaCompensation) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCompensation.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    checkoutSagaData.getOrderItems()
        .forEach(orderItem -> {
          // Check reservation exists or not
          ReservationEntity reservation = reservationRepository.findByOrderIdAndProductId(
                  checkoutSagaData.getOrderId(), orderItem.getProduct().getId())
              .orElseThrow(() -> new DomainException(DomainError.RESERVATION_NOT_FOUND));

          // Increase quantity to compensate
          productRepository.increaseQuantity(orderItem.getProduct().getId(),
              orderItem.getQuantity());

          // Delete reservation
          reservationRepository.delete(reservation);
        });

    return SagaCompensationResult.success();
  }
}
