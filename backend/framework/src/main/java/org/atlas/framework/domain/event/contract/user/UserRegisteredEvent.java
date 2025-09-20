package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.domain.event.DomainEventType;

@Getter
@Setter
public class UserRegisteredEvent extends BaseUserEvent {

  private Integer userId;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;

  public UserRegisteredEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.USER_REGISTERED;
  }
}
