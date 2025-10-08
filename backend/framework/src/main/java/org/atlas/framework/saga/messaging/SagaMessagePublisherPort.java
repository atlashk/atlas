package org.atlas.framework.saga.messaging;

import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;

public interface SagaMessagePublisherPort {

  void publish(SagaCommand command);

  void publish(SagaCommandReply reply);

  void publish(SagaCompensation compensation);

  void publish(SagaCompensationReply reply);
}
