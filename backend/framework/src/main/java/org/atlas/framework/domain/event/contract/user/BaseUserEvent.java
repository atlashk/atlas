package org.atlas.framework.domain.event.contract.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@NoArgsConstructor
@Getter
@Setter
public abstract class BaseUserEvent extends DomainEvent {

  protected Integer userId;

  public BaseUserEvent(String eventSource) {
    super(eventSource);
  }
}
