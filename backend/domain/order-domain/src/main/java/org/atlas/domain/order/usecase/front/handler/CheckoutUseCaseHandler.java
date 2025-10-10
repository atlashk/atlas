package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.orchestrator.DefaultSagaOrchestrator;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class CheckoutUseCaseHandler {

  private final DefaultSagaOrchestrator sagaOrchestratorManager;

  public Integer handle(CheckoutInput input) {
    return sagaOrchestratorManager.startSaga("checkout", SagaContext.of("input", input));
  }
}
