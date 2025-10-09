package org.atlas.domain.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.framework.saga.context.CheckoutSagaData;

@UtilityClass
public class OrderMapper {

  public static CheckoutSagaData toCheckoutSagaData(OrderEntity order) {
    // Map order basic info
    CheckoutSagaData data = new CheckoutSagaData();
    data.setOrderId(order.getId());
    data.setUserId(order.getUser().getId());
    data.setAmount(order.getAmount());
    data.setPaymentMethod(order.getPayment().getMethod());

    // Map order items
    if (order.getOrderItems() != null) {
      for (OrderItemEntity orderItemEntity : order.getOrderItems()) {
        CheckoutSagaData.OrderItem orderItem = new CheckoutSagaData.OrderItem();
        orderItem.setProductId(orderItemEntity.getProduct().getId());
        orderItem.setQuantity(orderItemEntity.getQuantity());
        data.addOrderItem(orderItem);
      }
    }

    return data;
  }
}
