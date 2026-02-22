package org.atlas.services.identity.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
