package org.atlas.libs.framework.domain.common.event.contract.identity;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.identity.UserRole;

@Getter
@Setter
public class UserUpdatedEvent extends DomainEvent {

  private String userId;
  private String firstName;
  private String lastName;
  private UserRole role;

  public UserUpdatedEvent() {
    super(DomainEventType.USER_UPDATED);
  }
}
