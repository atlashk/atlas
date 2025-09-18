package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@Getter
@Setter
public abstract class BaseProductEvent extends DomainEvent {

  public BaseProductEvent(String eventSource) {
    super(eventSource);
  }
}
