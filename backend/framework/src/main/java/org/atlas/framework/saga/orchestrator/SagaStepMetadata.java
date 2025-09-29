package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
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
public class SagaStepMetadata {

  private String stepName;
  private Object orchestratorInstance;
  private int stepOrder;
  private Method stepMethod;
  private Method compensateMethod;
}
