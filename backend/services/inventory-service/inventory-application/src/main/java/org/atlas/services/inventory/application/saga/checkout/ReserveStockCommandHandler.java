package org.atlas.services.inventory.application.saga.checkout;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.domain.shared.inventory.InsufficientStockException;
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
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.services.inventory.domain.entity.ReservationEntity;
import org.atlas.services.inventory.domain.entity.ReservationStatus;
import org.atlas.services.inventory.domain.entity.ReserveStockStrategy;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.domain.error.DomainError;
import org.atlas.services.inventory.domain.exception.DomainException;
import org.atlas.services.inventory.port.out.messaging.StockEventMessagePublisher;
import org.atlas.services.inventory.port.out.repository.ReservationRepository;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveStockCommandHandler {

  private final StockRepository stockRepository;
  private final ReservationRepository reservationRepository;
  private final ApplicationConfigService applicationConfigService;
  private final LockService lockService;
  private final SequenceGenerator sequenceGenerator;
  private final StockEventMessagePublisher stockEventMessagePublisher;

  @SagaCommandHandler(command = CheckoutCommand.RESERVE_STOCK)
  public SagaCommandResult reserveStock(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Try to reserve stock for each order item
    for (CheckoutSagaData.OrderItem orderItem : checkoutSagaData.getOrderItems()) {
      try {
        doReserveStock(orderItem.getProduct().getId(), orderItem.getQuantity());
      } catch (InsufficientStockException e) {
        log.error("Insufficient stock for product {}: {}",
            orderItem.getProduct().getId(), e.getMessage(), e);
        return SagaCommandResult.failure(
            String.format("Insufficient stock for product %s", orderItem.getProduct().getName()));
      } catch (Exception e) {
        log.error("Failed to reserve stock for product {}: {}", orderItem.getProduct().getId(),
            e.getMessage(), e);
        return SagaCommandResult.failure(String.format("Something went wrong with product %s",
            orderItem.getProduct().getName()));
      }

      // Insert new reservation
      ReservationEntity reservation = ReservationEntity.builder()
          .id(sequenceGenerator.generate(SequenceType.RESERVATION))
          .orderId(checkoutSagaData.getOrderId())
          .productId(orderItem.getProduct().getId())
          .quantity(orderItem.getQuantity())
          .status(ReservationStatus.ACTIVE)
          .build();
      reservationRepository.insert(reservation);
    }

    log.info("Successfully reserved products: sagaId={}, orderId={}",
        sagaCommand.getSagaId(), checkoutSagaData.getOrderId());
    return SagaCommandResult.success();
  }

  private void doReserveStock(String productId, Integer quantity)
      throws InsufficientStockException {
    ReserveStockStrategy reserveStockStrategy =
        applicationConfigService.getConfigAsClass("product.decrease-quantity-strategy",
            ReserveStockStrategy.class, ReserveStockStrategy.CONSTRAINT);
    switch (reserveStockStrategy) {
      case CONSTRAINT -> reserveStockWithConstraint(productId, quantity);
      case PESSIMISTIC_LOCK -> reserveStockWithPessimisticLock(productId, quantity);
      case OPTIMISTIC_LOCK -> reserveStockWithOptimisticLock(productId, quantity);
      case DISTRIBUTED_LOCK -> reserveStockWithDistributedLock(productId, quantity);
      default -> throw new UnsupportedOperationException(
          "Unsupported decrease quantity strategy: " + reserveStockStrategy);
    }
  }

  private void reserveStockWithConstraint(String productId, Integer quantity)
      throws InsufficientStockException {
    StockEntity reservedStock = stockRepository.reserveStockWithConstraint(productId, quantity);
    publishOutOfStockEventIfNeeded(reservedStock);
  }

  private void reserveStockWithPessimisticLock(String productId, Integer quantity)
      throws InsufficientStockException {
    StockEntity reservedStock = stockRepository.reserveStockWithPessimisticLock(productId,
        quantity);
    publishOutOfStockEventIfNeeded(reservedStock);
  }

  private void reserveStockWithOptimisticLock(String productId, Integer quantity)
      throws InsufficientStockException {
    StockEntity reservedStock = stockRepository.reserveStockWithOptimisticLock(productId, quantity);
    publishOutOfStockEventIfNeeded(reservedStock);
  }

  private void reserveStockWithDistributedLock(String productId, Integer quantity)
      throws InsufficientStockException {
    final String lockKey = String.format("product:%s:decrease-quantity", productId);
    final Duration waitTime = Duration.ofSeconds(5);
    final Duration leaseTime = Duration.ofSeconds(15);
    try {
      // Try to acquire distributed lock
      boolean acquiredLock = lockService.acquireLock(lockKey, waitTime, leaseTime);
      if (!acquiredLock) {
        throw new LockAcquisitionException("Failed to acquire lock: " + lockKey);
      }

      // Check stock availability
      StockEntity stock = stockRepository.findByProductId(productId)
          .orElseThrow(() -> new DomainException(DomainError.STOCK_NOT_FOUND));
      if (stock.getAvailableQuantity() < quantity) {
        throw new InsufficientStockException();
      }

      // Decrease available quantity and increase reserved quantity
      stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
      stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
      stockRepository.update(stock);

      // Publish stock status changed event if product is out of stock
      publishOutOfStockEventIfNeeded(stock);
    } finally {
      lockService.releaseLock(lockKey);
    }
  }

  @SagaCompensationHandler(command = CheckoutCommand.RESERVE_STOCK)
  public SagaCompensationResult compensateReserveStock(SagaCompensation sagaCompensation) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCompensation.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    final String orderId = checkoutSagaData.getOrderId();
    checkoutSagaData.getOrderItems()
        .forEach(orderItem -> {
          final String productId = orderItem.getProduct().getId();

          // Check reservation exists or not
          ReservationEntity reservation = reservationRepository.findByOrderIdAndProductId(
                  orderId, productId)
              .orElseThrow(() -> new DomainException(DomainError.RESERVATION_NOT_FOUND));

          StockEntity stock = stockRepository.findByProductId(productId)
              .orElseThrow(() -> new DomainException(DomainError.STOCK_NOT_FOUND));
          Integer currentAvailableQuantity = stock.getAvailableQuantity();

          // Release stock
          stockRepository.releaseStock(productId, orderItem.getQuantity());

          // Update reservation status to be RELEASED
          reservation.setStatus(ReservationStatus.RELEASED);
          reservationRepository.update(reservation);

          // Publish stock status changed event if product was previously out of stock and now has available quantity
          if (currentAvailableQuantity == 0) {
            StockStatusChangedEvent event = new StockStatusChangedEvent();
            event.setProductId(productId);
            event.setStockStatus(StockStatusChangedEvent.StockStatus.BACK_IN_STOCK);
            stockEventMessagePublisher.publish(event);
          }
        });

    return SagaCompensationResult.success();
  }

  private void publishOutOfStockEventIfNeeded(StockEntity stock) {
    if (stock.isOutOfStock()) {
      StockStatusChangedEvent event = new StockStatusChangedEvent();
      event.setProductId(stock.getProductId());
      event.setStockStatus(StockStatusChangedEvent.StockStatus.OUT_OF_STOCK);
      stockEventMessagePublisher.publish(event);
    }
  }
}
