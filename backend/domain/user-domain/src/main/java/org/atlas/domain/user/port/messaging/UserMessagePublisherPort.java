package org.atlas.domain.user.port.messaging;

import org.atlas.framework.domain.event.contract.user.BaseUserEvent;

public interface UserMessagePublisherPort {

  void publish(BaseUserEvent event);
}
