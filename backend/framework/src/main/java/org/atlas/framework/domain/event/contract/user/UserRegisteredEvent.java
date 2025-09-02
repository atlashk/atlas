package org.atlas.framework.domain.event.contract.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
