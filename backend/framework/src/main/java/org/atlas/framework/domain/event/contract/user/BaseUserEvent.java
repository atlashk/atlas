package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@Getter
@Setter
public abstract class BaseUserEvent extends DomainEvent {

  public BaseUserEvent(String eventSource) {
    super(eventSource);
  }
}
