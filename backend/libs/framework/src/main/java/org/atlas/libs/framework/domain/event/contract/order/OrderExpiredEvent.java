package org.atlas.libs.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.event.DomainEvent;
import org.atlas.libs.framework.domain.event.DomainEventType;

@Getter
@Setter
public class OrderExpiredEvent extends DomainEvent {

  private String orderId;

  public OrderExpiredEvent() {
    super(DomainEventType.ORDER_EXPIRED);
  }
}
