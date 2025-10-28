package org.atlas.framework.saga.core.orchestrator;

import org.atlas.framework.saga.core.context.SagaContext;
import org.atlas.framework.saga.core.entity.Saga;
import org.atlas.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.core.messaging.payload.SagaCompensationReply;

public interface SagaOrchestrator {

  /**
   * @return saga ID
   */
  Integer startSaga(String sagaName, SagaContext sagaContext);

  void sendCommand(Saga saga, String sagaCommandName, String targetServiceName);

  void createCommand(Integer sagaId, String sagaCommandName, String targetServiceName);

  void handleSagaCommandReply(SagaCommandReply reply);

  void handleSagaCompensationReply(SagaCompensationReply reply);

  void endSaga(Integer sagaId);

  void syncSagaContext(Integer sagaId, SagaContext newSagaContext);
}
