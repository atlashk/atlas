package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@NoArgsConstructor
@Getter
@Setter
public abstract class BaseProductEvent extends DomainEvent {

  protected Integer productId;

  public BaseProductEvent(String eventSource) {
    super(eventSource);
  }
}
