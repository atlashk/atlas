package org.atlas.framework.domain.event.contract.payment;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class PaymentFailedEvent extends BasePaymentEvent {

  private String error;

  public PaymentFailedEvent(String eventSource, Integer paymentId, Integer orderId) {
    super(eventSource, paymentId, orderId);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_FAILED;
  }
}
