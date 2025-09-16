package org.atlas.framework.messaging;

import org.atlas.framework.domain.event.DomainEvent;

public interface InternalMessagePublisherPort {

  void publish(DomainEvent event);
}
