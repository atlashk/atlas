package org.atlas.services.product.application.port.messaging;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductEvent event);
}
