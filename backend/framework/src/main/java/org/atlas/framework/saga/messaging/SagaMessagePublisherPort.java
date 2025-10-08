package org.atlas.framework.saga.messaging;

import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;

public interface SagaMessagePublisherPort {

  void publish(SagaCommand message);

  void publish(SagaCommandReply message);

  void publish(SagaCompensation message);

  void publish(SagaCompensationReply message);
}
