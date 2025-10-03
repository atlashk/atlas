package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.util.List;
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
public class SagaOrchestratorMetadata {

  private String sagaName;
  private Object orchestratorInstance;
  private Method startSagaMethod;
  private List<Method> sagaCommandReplyHandlerMethods;
}
