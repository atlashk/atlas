package org.atlas.framework.messaging;

import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;

public interface ExternalMessagePublisherPort {

  void publish(BaseOrderEvent event);

  void publish(BaseProductEvent event);

  void publish(BaseUserEvent event);

  void doPublish(Object messagePayload, String messageKey, String destination);
}
