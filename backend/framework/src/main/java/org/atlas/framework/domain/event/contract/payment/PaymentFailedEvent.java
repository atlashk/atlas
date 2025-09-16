package org.atlas.framework.domain.event.contract.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentFailedEvent extends BasePaymentEvent {

  private String error;

  public PaymentFailedEvent(String eventSource, String error) {
    super(eventSource);
    this.error = error;
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_FAILED;
  }
}
