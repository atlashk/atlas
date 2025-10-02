package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.util.ArrayList;
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

  private String orchestratorName;
  private Object orchestratorInstance;
  private List<SagaStepMetadata> steps = new ArrayList<>();
  private Method sagaCompletionHandler;
}
