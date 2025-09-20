package org.atlas.domain.order.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.CancellationReason;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.payment.PaymentCanceledEvent;
import org.atlas.framework.domain.event.contract.payment.PaymentCreatedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.InternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PAYMENT_CANCELED)
@RequiredArgsConstructor
@Slf4j
public class PaymentCanceledEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final InternalMessagePublisherPort internalMessagePublisherPort;

  public void handle(PaymentCanceledEvent event) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.AWAITING_PAYMENT) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Mark order as CANCELED
    orderEntity.setStatus(OrderStatus.CANCELED);
    orderEntity.setCancellationReason(CancellationReason.PAYMENT_CANCELED);
    orderRepository.update(orderEntity);

    // Publish event ORDER_CANCELED
    OrderCanceledEvent orderCanceledEvent = new OrderCanceledEvent(
        applicationConfigPort.getApplicationName());
    orderCanceledEvent.setOrderId(orderEntity.getId());
    orderCanceledEvent.setCancellationReason(orderEntity.getCancellationReason());
    internalMessagePublisherPort.publish(orderCanceledEvent);
  }
}