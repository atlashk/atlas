package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.model.User;

@Getter
@Setter
public class BaseUserEvent extends DomainEvent {

  private User user;

  public BaseUserEvent(DomainEventType eventType, User user) {
    super(eventType);
    this.user = user;
  }
}
