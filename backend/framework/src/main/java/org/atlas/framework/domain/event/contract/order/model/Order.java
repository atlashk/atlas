package org.atlas.framework.domain.event.contract.order.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.framework.domain.event.contract.user.model.User;

@Getter
@Setter
public class Order {

  private Integer id;
  private Integer userId;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Integer paymentId;
  private PaymentMethod paymentMethod;
  private String cancellationReason;
  private Date createdAt;

  public void addOrderItem(OrderItem orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItems.add(orderItem);
  }
}
