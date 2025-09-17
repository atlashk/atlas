package org.atlas.framework.domain.event.contract.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;

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
