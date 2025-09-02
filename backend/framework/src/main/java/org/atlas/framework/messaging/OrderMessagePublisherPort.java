package org.atlas.framework.messaging;

import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;

public interface OrderMessagePublisherPort {

  void publish(BaseOrderEvent event);
}
