package org.atlas.framework.domain.event.contract.order;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;
import org.atlas.framework.payment.model.nextaction.NextAction;

@Getter
@Setter
public class PaymentCreatedEvent extends BaseOrderEvent {

  private NextAction nextAction;

  public PaymentCreatedEvent(String eventSource, Order order) {
    super(eventSource, order);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_CREATED;
  }
}
