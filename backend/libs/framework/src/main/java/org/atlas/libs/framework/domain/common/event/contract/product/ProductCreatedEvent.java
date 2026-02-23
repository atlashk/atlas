package org.atlas.libs.framework.domain.common.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;

@Getter
@Setter
public class ProductCreatedEvent extends DomainEvent {

  private String productId;

  private String name;
  
  private Integer initialQuantity;

  public ProductCreatedEvent() {
    super(DomainEventType.PRODUCT_CREATED);
  }
}
