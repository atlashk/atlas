package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SagaCommandEvent extends SagaEvent {

  private String sagaCommandName;
  private String sagaContext;

  public SagaCommandEvent(String eventSource) {
    super(eventSource, SagaEventType.SAGA_COMMAND.name());
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
