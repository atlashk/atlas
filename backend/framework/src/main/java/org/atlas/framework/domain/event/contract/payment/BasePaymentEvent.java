package org.atlas.framework.domain.event.contract.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@NoArgsConstructor
@Getter
@Setter
public abstract class BasePaymentEvent extends DomainEvent {

  protected Integer paymentId;
  protected Integer orderId;

  public BasePaymentEvent(String eventSource, Integer paymentId, Integer orderId) {
    super(eventSource);
    this.paymentId = paymentId;
    this.orderId = orderId;
  }
}
