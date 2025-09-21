package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.model.User;

@Getter
@Setter
public class UserRegisteredEvent extends BaseUserEvent {

  public UserRegisteredEvent(String eventSource, User user) {
    super(eventSource, user);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.USER_REGISTERED;
  }
}
