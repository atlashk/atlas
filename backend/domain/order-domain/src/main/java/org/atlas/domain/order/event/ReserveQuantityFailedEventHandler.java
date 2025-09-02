package org.atlas.domain.order.event;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.port.messaging.OrderMessagePublisherPort;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.order.ReserveQuantityFailedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;

@DomainEventHandler(type = DomainEventType.RESERVE_QUANTITY_FAILED)
@RequiredArgsConstructor
public class ReserveQuantityFailedEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final OrderMessagePublisherPort orderMessagePublisherPort;

  public void handle(ReserveQuantityFailedEvent reserveQuantityFailedEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(reserveQuantityFailedEvent.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.PROCESSING) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order
    orderEntity.setStatus(OrderStatus.CANCELED);
    orderEntity.setCanceledReason(reserveQuantityFailedEvent.getError());
    orderRepository.update(orderEntity);

    // Publish event
    OrderCanceledEvent orderCanceledEvent = new OrderCanceledEvent(
        applicationConfigPort.getApplicationName());
    orderCanceledEvent.merge(reserveQuantityFailedEvent);
    orderCanceledEvent.setCanceledReason(orderEntity.getCanceledReason());
    orderMessagePublisherPort.publish(orderCanceledEvent);
  }
}
