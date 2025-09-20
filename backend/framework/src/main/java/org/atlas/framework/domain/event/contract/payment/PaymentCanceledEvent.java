package org.atlas.framework.domain.event.contract.payment;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class PaymentCanceledEvent extends BasePaymentEvent {

  private Integer orderId;
  private String cancellationReason;

  public PaymentCanceledEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_FAILED;
  }
}
