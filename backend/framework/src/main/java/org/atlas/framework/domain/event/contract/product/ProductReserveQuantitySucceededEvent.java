package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class ProductReserveQuantitySucceededEvent extends BaseProductEvent {

  private Integer orderId;

  public ProductReserveQuantitySucceededEvent(String eventSource, Integer productId) {
    super(eventSource, productId);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED;
  }
}
