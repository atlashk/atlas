package org.atlas.domain.product.infrastructure.messaging;

import org.atlas.framework.domain.event.contract.product.BaseProductEvent;

public interface ProductEventMessagePublisher {

  void publish(BaseProductEvent event);
}
