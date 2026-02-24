package org.atlas.libs.framework.domain.event.contract.inventory;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.event.DomainEvent;
import org.atlas.libs.framework.domain.event.DomainEventType;

@Getter
@Setter
public class StockStatusChangedEvent extends DomainEvent {

  private String productId;
  private StockStatus stockStatus;

  public StockStatusChangedEvent() {
    super(DomainEventType.STOCK_STATUS_CHANGED);
  }

  public enum StockStatus {

    OUT_OF_STOCK,
    BACK_IN_STOCK,
  }
}
