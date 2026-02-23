package org.atlas.libs.framework.domain.common.event.contract.identity;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;

@Getter
@Setter
public class UserDeletedEvent extends DomainEvent {

  private String userId;

  public UserDeletedEvent() {
    super(DomainEventType.USER_DELETED);
  }
}
