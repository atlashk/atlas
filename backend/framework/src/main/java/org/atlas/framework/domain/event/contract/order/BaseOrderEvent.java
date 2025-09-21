package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public abstract class BaseOrderEvent extends DomainEvent {

  protected Order order;

  public BaseOrderEvent(String eventSource, Order order) {
    super(eventSource);
    this.order = order;
  }
}
