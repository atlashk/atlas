package org.atlas.framework.saga.orchestrator;

import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;

public interface SagaOrchestrator {

  /**
   * @return saga ID
   */
  Long startSaga(String sagaName, SagaContext sagaContext);

  void sendCommand(Long sagaId, String sagaCommandName, String targetServiceName);

  void handleSagaCommandReply(SagaCommandReply reply);

  void handleSagaCompensationReply(SagaCompensationReply reply);

  void endSaga(Long sagaId);
}
