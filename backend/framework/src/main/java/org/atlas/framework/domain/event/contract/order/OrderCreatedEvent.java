package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public class OrderCreatedEvent extends BaseOrderEvent {

  public OrderCreatedEvent(String eventSource, Order order) {
    super(eventSource, order);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CREATED;
  }
}
