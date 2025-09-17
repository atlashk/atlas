package org.atlas.domain.order.event;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductReserveQuantitySucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED)
@RequiredArgsConstructor
public class ProductReserveQuantitySucceededEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort messagePublisherPort;

  public void handle(ProductReserveQuantitySucceededEvent productReserveQuantitySucceededEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(productReserveQuantitySucceededEvent.getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.PROCESSING) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order status
    orderEntity.setStatus(OrderStatus.RESERVED_QUANTITY);
    orderRepository.update(orderEntity);
  }
}
