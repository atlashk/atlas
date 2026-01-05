package org.atlas.product.application.port.messaging;

import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductEvent event);
}
