package org.atlas.framework.saga.lifecycle;

import org.atlas.framework.saga.command.SagaCommandType;
import org.atlas.framework.saga.context.SagaContext;

public interface SagaLifecycle {

  void startSaga(String sagaName, SagaContext sagaContext);

  void sendCommand(Long sagaId, SagaCommandType sagaCommandType);

  void rollbackSaga(Long sagaId, String errorMessage);

  void endSaga(Long sagaId);
}
