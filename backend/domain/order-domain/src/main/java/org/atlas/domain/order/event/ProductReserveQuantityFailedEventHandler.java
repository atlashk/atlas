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
import org.atlas.framework.messaging.InternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVE_QUANTITY_FAILED)
@RequiredArgsConstructor
public class ProductReserveQuantityFailedEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final InternalMessagePublisherPort internalMessagePublisherPort;

  public void handle(ProductReserveQuantityFailedEvent event) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(event.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.PROCESSING) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Mark order as CANCELED
    orderEntity.setStatus(OrderStatus.CANCELED);
    orderEntity.setCanceledReason("Failed to reserve product quantity");
    orderRepository.update(orderEntity);

    // Publish event ORDER_CANCELED
    publishOrderCanceledEvent(orderEntity);
  }

  private void publishOrderCanceledEvent(OrderEntity orderEntity) {
    OrderCanceledEvent orderCanceledEvent = new OrderCanceledEvent(
        applicationConfigPort.getApplicationName(),
        orderEntity.getId(),
        orderEntity.getCanceledReason()
    );
    internalMessagePublisherPort.publish(orderCanceledEvent);
  }
}
