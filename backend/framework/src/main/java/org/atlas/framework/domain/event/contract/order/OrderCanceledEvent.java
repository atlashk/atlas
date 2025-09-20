package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class OrderCanceledEvent extends BaseOrderEvent {

  private Integer orderId;
  private String cancellationReason;

  public OrderCanceledEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.ORDER_CANCELED;
  }
}
