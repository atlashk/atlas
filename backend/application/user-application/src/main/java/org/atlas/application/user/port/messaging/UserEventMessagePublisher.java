package org.atlas.application.user.port.messaging;

import org.atlas.framework.domain.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
