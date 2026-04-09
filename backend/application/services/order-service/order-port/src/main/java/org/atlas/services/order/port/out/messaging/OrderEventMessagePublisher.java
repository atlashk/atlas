package org.atlas.services.order.port.out.messaging;

import org.atlas.libs.framework.domain.event.contract.order.OrderExpiredEvent;

public interface OrderEventMessagePublisher {

  void publish(OrderExpiredEvent event);
}
