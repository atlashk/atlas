package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public class OrderCanceledEvent extends BaseOrderEvent {

  public OrderCanceledEvent(String eventSource, Order order) {
    super(eventSource, order);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CANCELED;
  }
}
