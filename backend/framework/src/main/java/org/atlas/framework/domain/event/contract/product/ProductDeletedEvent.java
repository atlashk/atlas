package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class ProductDeletedEvent extends BaseProductEvent {

  private Integer productId;

  public ProductDeletedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_DELETED;
  }
}
