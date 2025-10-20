package org.atlas.framework.saga.messaging;

import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;

public interface SagaMessagePublisher {

  void publish(SagaCommand sagaCommand);

  void publish(SagaCommandReply sagaCommandReply);

  void publish(SagaCompensation sagaCompensation);

  void publish(SagaCompensationReply sagaCompensationReply);
}
