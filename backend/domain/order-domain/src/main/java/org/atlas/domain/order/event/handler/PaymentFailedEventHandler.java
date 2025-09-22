package org.atlas.domain.order.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.mapper.OrderEventMapper;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.CancellationReason;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.order.PaymentFailedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.messaging.InternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PAYMENT_FAILED)
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final InternalMessagePublisherPort internalMessagePublisherPort;

  public void handle(PaymentFailedEvent paymentFailedEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(paymentFailedEvent.getOrder().getId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.AWAITING_PAYMENT) {
      throw new DomainException(DomainError.ORDER_INVALID_STATUS);
    }

    // Mark order as CANCELED
    orderEntity.setStatus(OrderStatus.CANCELED);
    orderEntity.setCancellationReason(CancellationReason.PAYMENT_FAILED);
    orderRepository.update(orderEntity);

    // Publish event ORDER_CANCELED
    OrderCanceledEvent orderCanceledEvent = new OrderCanceledEvent(
        applicationConfigPort.getApplicationName(),
        OrderEventMapper.fromOrderEntity(orderEntity)
    );
    internalMessagePublisherPort.publish(orderCanceledEvent);
  }
}