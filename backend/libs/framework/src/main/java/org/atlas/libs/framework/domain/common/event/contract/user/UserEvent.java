package org.atlas.libs.framework.domain.common.event.contract.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.user.UserRole;

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
  private UserRole role;

  public UserEvent(DomainEventType eventType) {
    super(eventType);
  }
}
