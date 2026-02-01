package org.atlas.services.iam.port.out.messaging;

import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;

public interface UserEventMessagePublisher {

  void publish(UserEvent event);
}
