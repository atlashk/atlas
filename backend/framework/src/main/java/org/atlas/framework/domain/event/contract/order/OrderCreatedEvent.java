package org.atlas.framework.domain.event.contract.order;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderCreatedEvent extends BaseOrderEvent {

  public OrderCreatedEvent(String eventSource) {
    super(eventSource);
  }

  public void addOrderItem(OrderItem orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItems.add(orderItem);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CREATED;
  }
}
