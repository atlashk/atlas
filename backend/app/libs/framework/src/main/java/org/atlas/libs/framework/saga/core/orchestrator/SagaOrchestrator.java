package org.atlas.libs.framework.saga.core.orchestrator;

import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.entity.SagaEntity;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensationReply;

public interface SagaOrchestrator {

  /**
   * @return saga ID
   */
  Integer runSaga(String sagaName, SagaContext sagaContext);

  void sendSagaCommand(SagaEntity saga, String sagaCommandName, String targetServiceName);

  void createSagaCommand(Integer sagaId, String sagaCommandName, String targetServiceName);

  void handleSagaCommandReply(SagaCommandReply reply);

  void handleSagaCompensationReply(SagaCompensationReply reply);

  void rollbackSaga(Integer sagaId);

  void endSaga(Integer sagaId);
}
