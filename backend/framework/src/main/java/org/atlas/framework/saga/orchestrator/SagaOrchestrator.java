package org.atlas.framework.saga.orchestrator;

import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.event.SagaCompensationReplyEvent;
import org.atlas.framework.saga.event.SagaCommandReplyEvent;

public interface SagaOrchestrator {

  /**
   * @return saga ID
   */
  Long startSaga(String sagaName, SagaContext sagaContext);

  void sendCommand(Long sagaId, String sagaCommandName);

  void handleSagaCommandReplyEvent(SagaCommandReplyEvent event);

  void handleSagaCompensationReplyEvent(SagaCompensationReplyEvent event);

  void endSaga(Long sagaId);
}
