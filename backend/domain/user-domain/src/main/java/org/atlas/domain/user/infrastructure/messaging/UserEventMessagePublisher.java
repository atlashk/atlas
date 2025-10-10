package org.atlas.domain.user.infrastructure.messaging;

import org.atlas.framework.domain.event.contract.user.BaseUserEvent;

public interface UserEventMessagePublisher {

  void publish(BaseUserEvent event);
}
