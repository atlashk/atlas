package org.atlas.framework.notification.realtime.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.order.OrderConfirmedEvent;

@Data
@NoArgsConstructor
public class OrderStatusChangedPayload {

  private Integer orderId;
  private OrderStatus orderStatus;
  private String canceledReason;

  public static OrderStatusChangedPayload from(OrderConfirmedEvent event) {
    OrderStatusChangedPayload instance = new OrderStatusChangedPayload();
    instance.setOrderId(event.getOrderId());
    instance.setOrderStatus(OrderStatus.CONFIRMED);
    return instance;
  }

  public static OrderStatusChangedPayload from(OrderCanceledEvent event) {
    OrderStatusChangedPayload instance = new OrderStatusChangedPayload();
    instance.setOrderId(event.getOrderId());
    instance.setOrderStatus(OrderStatus.CANCELED);
    instance.setCanceledReason(event.getCanceledReason());
    return instance;
  }
}