package org.atlas.framework.domain.event.contract.product;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUpdatedEvent extends BaseProductEvent {

  private String name;
  private BigDecimal price;

  public ProductUpdatedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_UPDATED;
  }
}
