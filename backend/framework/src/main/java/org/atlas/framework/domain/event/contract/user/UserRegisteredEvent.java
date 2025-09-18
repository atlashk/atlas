package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class UserRegisteredEvent extends BaseUserEvent {

  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;

  public UserRegisteredEvent(String eventSource, Integer userId) {
    super(eventSource, userId);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.USER_REGISTERED;
  }
}
