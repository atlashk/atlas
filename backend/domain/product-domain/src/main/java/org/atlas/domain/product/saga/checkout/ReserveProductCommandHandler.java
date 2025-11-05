package org.atlas.domain.product.saga.checkout;

import java.time.Duration;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.entity.Reservation;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.ReservationRepository;
import org.atlas.domain.product.shared.DecreaseQuantityStrategy;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.exception.OutOfStockException;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockService;
import org.atlas.framework.saga.checkout.CheckoutCommand;
import org.atlas.framework.saga.checkout.CheckoutSagaData;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.annotation.SagaCompensationHandler;
import org.atlas.framework.saga.core.command.SagaCommandResult;
import org.atlas.framework.saga.core.compensation.SagaCompensationResult;
import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.saga.core.messaging.payload.SagaCompensation;
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
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
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
        log.error("Failed to reserve product {}: {}", orderItem.getProduct().getId(), e.getMessage(), e);
        return SagaCommandResult.failure("Something went wrong with product %s",
            orderItem.getProduct().getName());
      }

      // Insert new reservation
      Reservation reservation = Reservation.builder()
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

  private void decreaseQuantity(Integer productId, Integer quantity)
      throws OutOfStockException {
    DecreaseQuantityStrategy decreaseQuantityStrategy =
        applicationConfigService.getConfigAsClass("product.decrease-quantity-strategy",
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
          boolean acquiredLock = lockService.acquireLock(lockKey, waitTime, leaseTime);
          if (!acquiredLock) {
            throw new LockAcquisitionException("Failed to acquire lock: " + lockKey);
          }
          Product product = productRepository.findById(productId)
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
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + decreaseQuantityStrategy);
    }
  }

  @SagaCompensationHandler(command = CheckoutCommand.RESERVE_PRODUCT)
  public SagaCompensationResult compensateReserveProduct(SagaCompensation sagaCompensation) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCompensation.getSagaContext());
    CheckoutSagaData checkoutSagaData = JsonUtil.getInstance().toObject(
        sagaContext.get("data", LinkedHashMap.class), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    checkoutSagaData.getOrderItems()
        .forEach(orderItem -> {
          // Check reservation exists or not
          Reservation reservation = reservationRepository.findByOrderIdAndProductId(
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
