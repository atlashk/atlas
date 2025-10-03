package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCommandEvent {

  private Long sagaId;
  private String commandName;
  private String sagaContext;
}
