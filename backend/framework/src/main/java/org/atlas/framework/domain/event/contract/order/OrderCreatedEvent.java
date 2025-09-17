package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class OrderCreatedEvent extends BaseOrderEvent {

  public OrderCreatedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CREATED;
  }
}
