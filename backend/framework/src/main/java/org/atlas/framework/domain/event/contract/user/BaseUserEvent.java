package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@Getter
@Setter
public abstract class BaseUserEvent extends DomainEvent {

  protected Integer userId;

  public BaseUserEvent(String eventSource, Integer userId) {
    super(eventSource);
    this.userId = userId;
  }
}
