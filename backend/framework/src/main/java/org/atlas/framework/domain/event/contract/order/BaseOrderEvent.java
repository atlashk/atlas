package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.domain.event.DomainEvent;

@Getter
@Setter
public abstract class BaseOrderEvent extends DomainEvent {

  protected Order order;

  public BaseOrderEvent(String eventSource) {
    super(eventSource);
  }
}
