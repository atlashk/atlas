package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;

@Getter
@Setter
public abstract class BaseProductEvent extends DomainEvent {

  protected Product product;

  public BaseProductEvent(String eventSource, Product product) {
    super(eventSource);
    this.product = product;
  }
}
