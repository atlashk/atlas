package org.atlas.framework.domain.event.contract.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;

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
