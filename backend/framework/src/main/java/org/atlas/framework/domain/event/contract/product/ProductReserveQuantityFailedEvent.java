package org.atlas.framework.domain.event.contract.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductReserveQuantityFailedEvent extends BaseProductEvent {

  private Integer orderId;

  public ProductReserveQuantityFailedEvent(String eventSource, Integer orderId) {
    super(eventSource);
    this.orderId = orderId;
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_RESERVE_QUANTITY_FAILED;
  }
}
