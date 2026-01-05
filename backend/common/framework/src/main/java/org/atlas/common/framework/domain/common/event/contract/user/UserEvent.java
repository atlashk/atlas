package org.atlas.common.framework.domain.common.event.contract.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.common.event.DomainEvent;
import org.atlas.common.framework.domain.common.event.DomainEventType;
import org.atlas.common.framework.domain.user.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserEvent extends DomainEvent {

  private Integer userId;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;

  public UserEvent(DomainEventType eventType) {
    super(eventType);
  }
}
