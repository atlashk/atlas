package org.atlas.libs.framework.domain.common.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;

@Getter
@Setter
public class ProductDeletedEvent extends DomainEvent {

  private String productId;

  public ProductDeletedEvent() {
    super(DomainEventType.PRODUCT_DELETED);
  }
}
