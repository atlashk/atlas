package org.atlas.domain.user.infrastructure.messaging;

import org.atlas.framework.domain.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
