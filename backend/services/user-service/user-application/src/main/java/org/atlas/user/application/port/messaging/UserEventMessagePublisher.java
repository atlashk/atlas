package org.atlas.user.application.port.messaging;

import org.atlas.common.framework.domain.common.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
