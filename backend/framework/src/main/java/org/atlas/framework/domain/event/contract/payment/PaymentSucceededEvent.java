package org.atlas.framework.domain.event.contract.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.User;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentSucceededEvent extends BasePaymentEvent {

  public PaymentSucceededEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_SUCCEEDED;
  }
}
