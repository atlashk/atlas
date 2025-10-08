package org.atlas.domain.product.port.messaging;

import org.atlas.framework.domain.event.contract.product.BaseProductEvent;

public interface ProductMessagePublisherPort {

  void publish(BaseProductEvent event);
}
