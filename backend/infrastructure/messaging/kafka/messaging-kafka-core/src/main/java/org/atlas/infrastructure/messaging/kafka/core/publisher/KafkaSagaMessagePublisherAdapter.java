package org.atlas.infrastructure.messaging.kafka.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.saga.messaging.SagaMessagePublisherPort;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaSagaMessagePublisherAdapter implements SagaMessagePublisherPort {

  private final MessagePublisherPort messagePublisherPort;

  @Override
  public void publish(SagaCommand command) {
    PublishRequest request = PublishRequest.builder()
        .destination(String.format("saga.%s.command.%s",
            command.getSagaName(), command.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", command.getSagaId()))
        .messagePayload(command)
        .build();
    messagePublisherPort.publish(request);
  }

  @Override
  public void publish(SagaCommandReply reply) {
    PublishRequest request = PublishRequest.builder()
        .destination(String.format("saga.%s.commandreply", reply.getSagaName()))
        .routingAttributes(Map.of("messageKey", reply.getSagaId()))
        .messagePayload(reply)
        .build();
    messagePublisherPort.publish(request);
  }

  @Override
  public void publish(SagaCompensation compensation) {
    PublishRequest request = PublishRequest.builder()
        .destination(String.format("saga.%s.compensation.%s",
            compensation.getSagaName(), compensation.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", compensation.getSagaId()))
        .messagePayload(compensation)
        .build();
    messagePublisherPort.publish(request);
  }

  @Override
  public void publish(SagaCompensationReply reply) {
    PublishRequest request = PublishRequest.builder()
        .destination(String.format("saga.%s.compensationreply", reply.getSagaName()))
        .routingAttributes(Map.of("messageKey", reply.getSagaId()))
        .messagePayload(reply)
        .build();
    messagePublisherPort.publish(request);
  }
}
