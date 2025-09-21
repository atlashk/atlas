package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.user.model.User;

@Getter
@Setter
public abstract class BaseUserEvent extends DomainEvent {

  protected User user;

  public BaseUserEvent(String eventSource, User user) {
    super(eventSource);
    this.user = user;
  }
}
