package org.atlas.framework.domain.event.contract.product;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public class ProductReserveQuantitySucceededEvent extends BaseProductEvent {

  private Order order;

  public ProductReserveQuantitySucceededEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PRODUCT_RESERVE_QUANTITY_SUCCEEDED;
  }
}
