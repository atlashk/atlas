package org.atlas.framework.saga.orchestrator;

import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;

public interface SagaOrchestrator {

  /**
   * @return saga ID
   */
  Integer startSaga(String sagaName, SagaContext sagaContext);

  void sendCommand(SagaEntity sagaEntity, String sagaCommandName, String targetServiceName);

  void createCommand(Integer sagaId, String sagaCommandName, String targetServiceName);

  void handleSagaCommandReply(SagaCommandReply reply);

  void handleSagaCompensationReply(SagaCompensationReply reply);

  void endSaga(Integer sagaId);

  void syncSagaContext(Integer sagaId, SagaContext newSagaContext);
}
