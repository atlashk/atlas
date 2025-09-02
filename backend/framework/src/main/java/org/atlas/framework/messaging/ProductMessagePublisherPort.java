package org.atlas.framework.messaging;

import org.atlas.framework.domain.event.contract.product.BaseProductEvent;

public interface ProductMessagePublisherPort {

  void publish(BaseProductEvent event);
}
