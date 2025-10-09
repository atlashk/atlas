package org.atlas.infrastructure.messaging.sns.core.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.saga.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnsSagaMessagePublisher implements SagaMessagePublisher {

  private final SnsProps snsProps;
  private final MessagePublisher messagePublisher;

  @Override
  public void publish(SagaCommand command) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.command.%s",
                command.getSagaName(), command.getTargetServiceName())))
        .messagePayload(command)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCommandReply reply) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.commandreply", reply.getSagaName())))
        .messagePayload(reply)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCompensation compensation) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.compensation.%s",
                compensation.getSagaName(), compensation.getTargetServiceName())))
        .messagePayload(compensation)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCompensationReply reply) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn()
            .get(String.format("saga.%s.compensationreply", reply.getSagaName())))
        .messagePayload(reply)
        .build();
    messagePublisher.publish(request);
  }
}
