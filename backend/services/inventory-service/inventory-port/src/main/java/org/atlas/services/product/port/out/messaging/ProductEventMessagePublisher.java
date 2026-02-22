package org.atlas.services.product.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;

public interface ProductEventMessagePublisher {

  void publish(ProductEvent event);
}
