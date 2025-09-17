package org.atlas.domain.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.payment.PaymentIntentCreatedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;

@DomainEventHandler(type = DomainEventType.PAYMENT_INTENT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class PaymentIntentCreatedEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;

  public void handle(PaymentIntentCreatedEvent event) {
    log.info("Handling PaymentIntentCreatedEvent for order: {}", event.getOrderId());
    
    try {
      // Find order
      OrderEntity orderEntity = orderRepository.findById(event.getOrderId())
          .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
      
      // Validate order status
      if (orderEntity.getStatus() != OrderStatus.RESERVED_QUANTITY) {
        log.warn("Order {} has invalid status for payment intent creation: {}", 
            event.getOrderId(), orderEntity.getStatus());
        throw new DomainException(AppError.ORDER_INVALID_STATUS);
      }

      // Update order status to AWAITING_PAYMENT
      orderEntity.setStatus(OrderStatus.AWAITING_PAYMENT);
      orderRepository.update(orderEntity);
      
      log.info("Successfully updated order {} status to AWAITING_PAYMENT", event.getOrderId());
      
      // TODO: Optionally notify frontend via WebSocket/SSE that payment is ready
      // notifyFrontendPaymentReady(event);
      
    } catch (DomainException e) {
      log.error("Domain error handling PaymentIntentCreatedEvent for order: {}", 
          event.getOrderId(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error handling PaymentIntentCreatedEvent for order: {}", 
          event.getOrderId(), e);
      throw new RuntimeException("Failed to handle PaymentIntentCreatedEvent", e);
    }
  }
}