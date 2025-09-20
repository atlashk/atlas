package org.atlas.domain.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.Product;
import org.atlas.framework.domain.event.contract.order.model.User;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UtilityClass
public class OrderEventMapper {

  public static Order fromOrderEntity(OrderEntity orderEntity) {
    // Map basic fields
    Order order = new Order();
    order.setOrderId(orderEntity.getId());
    order.setAmount(orderEntity.getAmount());
    order.setPaymentMethod(orderEntity.getPayment().getMethod());
    order.setCreatedAt(orderEntity.getCreatedAt());

    // Map user
    if (orderEntity.getUser() != null) {
      order.setUser(ObjectMapperUtil.getInstance().map(orderEntity.getUser(), User.class));
    }

    // Map order items
    if (orderEntity.getOrderItems() != null) {
      for (OrderItemEntity orderItemEntity : orderEntity.getOrderItems()) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(
            ObjectMapperUtil.getInstance().map(orderItemEntity.getProduct(), Product.class));
        orderItem.setQuantity(orderItemEntity.getQuantity());
        order.addOrderItem(orderItem);
      }
    }

    return order;
  }
}
