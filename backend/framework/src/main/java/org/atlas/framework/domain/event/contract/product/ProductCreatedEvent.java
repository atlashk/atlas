package org.atlas.framework.domain.event.contract.product;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductCreatedEvent extends DomainEvent {

  private Integer productId;
  private String name;
  private BigDecimal price;

  public ProductCreatedEvent(String eventSource) {
    super(eventSource);
  }
}
