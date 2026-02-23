package org.atlas.services.catalog.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductCreatedEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductCreatedEvent event);
}
