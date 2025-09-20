package org.atlas.framework.domain.event.contract.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@NoArgsConstructor
@Getter
@Setter
public abstract class BasePaymentEvent extends DomainEvent {

  public BasePaymentEvent(String eventSource) {
    super(eventSource);
  }
}
