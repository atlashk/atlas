package org.atlas.domain.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.OrderItemEntity;
import org.atlas.framework.saga.context.CheckoutSagaData;

@UtilityClass
public class OrderMapper {

  public static CheckoutSagaData toCheckoutSagaData(OrderEntity orderEntity) {
    // Map order basic info
    CheckoutSagaData data = new CheckoutSagaData();
    data.setOrderId(orderEntity.getId());
    data.setUserId(orderEntity.getUser().getId());
    data.setAmount(orderEntity.getAmount());
    data.setPaymentMethod(orderEntity.getPayment().getMethod());

    // Map order items
    if (orderEntity.getOrderItems() != null) {
      for (OrderItemEntity orderItemEntity : orderEntity.getOrderItems()) {
        CheckoutSagaData.OrderItem orderItem = new CheckoutSagaData.OrderItem();
        orderItem.setProductId(orderItemEntity.getProduct().getId());
        orderItem.setQuantity(orderItemEntity.getQuantity());
        data.addOrderItem(orderItem);
      }
    }

    return data;
  }
}
