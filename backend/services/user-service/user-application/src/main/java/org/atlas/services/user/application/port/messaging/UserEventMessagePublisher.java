package org.atlas.services.user.application.port.messaging;

import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
