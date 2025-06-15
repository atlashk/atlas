package org.atlas.framework.domain.event.contract.product;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.User;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReserveQuantitySucceededEvent extends DomainEvent {

  private Integer orderId;
  private User user;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Date createdAt;

  public ReserveQuantitySucceededEvent(String eventSource) {
    super(eventSource);
  }

  public void merge(OrderCreatedEvent event) {
    this.orderId = event.getOrderId();
    this.user = new User(event.getUser());
    this.orderItems = event.getOrderItems() // Deep copy
        .stream()
        .map(OrderItem::new)
        .toList();
    this.amount = event.getAmount();
    this.createdAt = event.getCreatedAt();
  }
}
