package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SagaCompensationEvent extends SagaEvent {

  private String sagaCommandName;
  private String sagaContext;

  public SagaCompensationEvent(String eventSource) {
    super(eventSource, SagaEventType.SAGA_COMPENSATION.name());
  }

  @Override
  public String toString() {
    return "{" +
        "sagaId=" + sagaId +
        ", sagaName='" + sagaName + '\'' +
        ", sagaCommandName='" + sagaCommandName + '\'' +
        '}';
  }
}