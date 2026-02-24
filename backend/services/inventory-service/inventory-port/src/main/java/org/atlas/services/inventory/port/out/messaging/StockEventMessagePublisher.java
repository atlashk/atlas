package org.atlas.services.inventory.port.out.messaging;

import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;

public interface StockEventMessagePublisher {

  void publish(StockStatusChangedEvent event);
}
