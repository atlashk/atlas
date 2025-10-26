package org.atlas.domain.order.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.vo.OrderItemVO;
import org.atlas.framework.saga.context.model.CheckoutSagaData;

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
      for (OrderItemVO orderItem : order.getOrderItems()) {
        CheckoutSagaData.OrderItem orderItemData = new CheckoutSagaData.OrderItem();
        orderItemData.setProductId(orderItem.getProduct().getId());
        orderItemData.setQuantity(orderItem.getQuantity());
        data.addOrderItem(orderItemData);
      }
    }

    return data;
  }
}
