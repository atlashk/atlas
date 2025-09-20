package org.atlas.domain.order.event.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductReservationSucceededEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;

@DomainEventHandler(type = DomainEventType.PRODUCT_RESERVATION_SUCCEEDED)
@RequiredArgsConstructor
public class ProductReservationSucceededEventHandler {

  private final OrderRepository orderRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public void handle(ProductReservationSucceededEvent productReservationSucceededEvent) {
    // Find order
    OrderEntity orderEntity = orderRepository.findById(
            productReservationSucceededEvent.getOrder().getOrderId())
        .orElseThrow(() -> new DomainException(AppError.ORDER_NOT_FOUND));
    if (orderEntity.getStatus() != OrderStatus.AWAITING_PRODUCT_RESERVATION) {
      throw new DomainException(AppError.ORDER_INVALID_STATUS);
    }

    // Update order status
    orderEntity.setStatus(OrderStatus.PRODUCT_RESERVATION_SUCCEEDED);
    orderRepository.update(orderEntity);
  }
}
