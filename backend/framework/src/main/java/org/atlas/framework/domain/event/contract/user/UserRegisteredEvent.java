package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.model.User;

@Getter
@Setter
public class UserRegisteredEvent extends BaseUserEvent {

  public UserRegisteredEvent(User user) {
    super(DomainEventType.USER_REGISTERED, user);
  }
}
