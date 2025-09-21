package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.model.Product;

@Getter
@Setter
public class ProductCreatedEvent extends BaseProductEvent {

  public ProductCreatedEvent(String eventSource, Product product) {
    super(eventSource, product);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_CREATED;
  }
}
