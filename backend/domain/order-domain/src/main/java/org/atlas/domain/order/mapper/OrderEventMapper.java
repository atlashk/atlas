package org.atlas.domain.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;

@UtilityClass
public class OrderEventMapper {

  public static Order fromOrderEntity(OrderEntity orderEntity) {
    // Map order basic info
    Order order = new Order();
    order.setId(orderEntity.getId());
    order.setAmount(orderEntity.getAmount());
    order.setPaymentMethod(orderEntity.getPayment().getMethod());
    order.setCreatedAt(orderEntity.getCreatedAt());

    // Map user
    order.setUserId(orderEntity.getUser().getId());

    // Map order items
    if (orderEntity.getOrderItems() != null) {
      for (OrderItemEntity orderItemEntity : orderEntity.getOrderItems()) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(orderItemEntity.getProduct().getId());
        orderItem.setQuantity(orderItemEntity.getQuantity());
        order.addOrderItem(orderItem);
      }
    }

    return order;
  }
}
