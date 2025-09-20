package org.atlas.framework.domain.event.contract.payment;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class PaymentSucceededEvent extends BasePaymentEvent {

  private Integer orderId;

  public PaymentSucceededEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_SUCCEEDED;
  }
}
