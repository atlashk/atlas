package org.atlas.framework.domain.event.contract.order.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.enums.PaymentGateway;

@Getter
@Setter
public class Order {

  private Integer orderId;
  private User user;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private PaymentGateway paymentGateway;
  private Date createdAt;

  public void addOrderItem(OrderItem orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItems.add(orderItem);
  }
}
