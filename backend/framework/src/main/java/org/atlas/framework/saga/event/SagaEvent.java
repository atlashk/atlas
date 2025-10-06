package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SagaEvent extends DomainEvent {

  protected Long sagaId;
  protected String sagaName;

  public SagaEvent(String eventSource, String eventType) {
    super(eventSource, eventType);
  }
}
