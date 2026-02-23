package org.atlas.services.identity.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.identity.UserCreatedEvent;

public interface UserEventMessagePublisher {

  void publish(UserCreatedEvent event);
}
