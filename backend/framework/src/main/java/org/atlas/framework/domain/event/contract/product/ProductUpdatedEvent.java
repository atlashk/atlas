package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.model.Product;

@Getter
@Setter
public class ProductUpdatedEvent extends BaseProductEvent {

  public ProductUpdatedEvent(Product product) {
    super(DomainEventType.PRODUCT_UPDATED, product);
  }
}
