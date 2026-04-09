package org.atlas.services.catalog.port.out.messaging;

import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductCreatedEvent event);
}
