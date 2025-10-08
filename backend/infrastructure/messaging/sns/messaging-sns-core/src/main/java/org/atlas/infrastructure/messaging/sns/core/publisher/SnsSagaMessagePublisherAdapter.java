package org.atlas.infrastructure.messaging.sns.core.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.saga.messaging.SagaMessagePublisherPort;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnsSagaMessagePublisherAdapter implements SagaMessagePublisherPort {

  private final SnsProps snsProps;
  private final MessagePublisherPort messagePublisherPort;

  @Override
  public void publish(SagaCommand message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.command.%s",
                message.getSagaName(), message.getTargetServiceName())))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCommandReply message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.commandreply", message.getSagaName())))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensation message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.compensation.%s",
                message.getSagaName(), message.getTargetServiceName())))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensationReply message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.compensationreply", message.getSagaName())))
        .build();
    messagePublisherPort.publish(destination, message);
  }
}
