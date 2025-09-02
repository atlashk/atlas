package org.atlas.framework.domain.event.contract.order;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.User;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderConfirmedEvent extends BaseOrderEvent {

  public OrderConfirmedEvent(String eventSource) {
    super(eventSource);
  }

  public void merge(ReserveQuantitySucceededEvent event) {
    this.orderId = event.getOrderId();
    this.user = new User(event.getUser());
    this.orderItems = event.getOrderItems() // Deep copy
        .stream()
        .map(OrderItem::new)
        .toList();
    this.amount = event.getAmount();
    this.createdAt = event.getCreatedAt();
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CONFIRMED;
  }
}
