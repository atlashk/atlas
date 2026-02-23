package org.atlas.services.product.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductCreatedEvent;

public interface StockEventMessagePublisher {

  void publish(ProductCreatedEvent event);
}
