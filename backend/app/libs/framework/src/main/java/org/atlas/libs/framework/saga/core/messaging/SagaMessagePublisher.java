package org.atlas.libs.framework.saga.core.messaging;

import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensationReply;

public interface SagaMessagePublisher {

  void publish(SagaCommand sagaCommand);

  void publish(SagaCommandReply sagaCommandReply);

  void publish(SagaCompensation sagaCompensation);

  void publish(SagaCompensationReply sagaCompensationReply);
}
