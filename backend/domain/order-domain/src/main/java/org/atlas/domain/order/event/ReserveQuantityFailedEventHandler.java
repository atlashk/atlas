package org.atlas.domain.order.event;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.product.ProductReserveQuantityFailedEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.RESERVE_QUANTITY_FAILED)
@RequiredArgsConstructor
public class ReserveQuantityFailedEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort messagePublisherPort;

  public void handle(ProductReserveQuantityFailedEvent productReserveQuantityFailedEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(productReserveQuantityFailedEvent.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.PROCESSING) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order
    orderEntity.setStatus(OrderStatus.CANCELED);
    orderEntity.setCanceledReason(productReserveQuantityFailedEvent.getError());
    orderRepository.update(orderEntity);

    // Publish event
    OrderCanceledEvent orderCanceledEvent = new OrderCanceledEvent(
        applicationConfigPort.getApplicationName());
    orderCanceledEvent.merge(productReserveQuantityFailedEvent);
    orderCanceledEvent.setCanceledReason(orderEntity.getCanceledReason());
    messagePublisherPort.publish(orderCanceledEvent);
  }
}
