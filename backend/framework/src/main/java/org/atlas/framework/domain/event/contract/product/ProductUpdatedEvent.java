package org.atlas.framework.domain.event.contract.product;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class ProductUpdatedEvent extends BaseProductEvent {

  private Integer productId;
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
