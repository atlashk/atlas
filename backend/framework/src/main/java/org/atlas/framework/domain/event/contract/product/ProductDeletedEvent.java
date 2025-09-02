package org.atlas.framework.domain.event.contract.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDeletedEvent extends BaseProductEvent {

  public ProductDeletedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_DELETED;
  }
}
