package org.atlas.domain.product.infrastructure.messaging;

import org.atlas.framework.domain.event.contract.product.ProductEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductEvent event);
}
