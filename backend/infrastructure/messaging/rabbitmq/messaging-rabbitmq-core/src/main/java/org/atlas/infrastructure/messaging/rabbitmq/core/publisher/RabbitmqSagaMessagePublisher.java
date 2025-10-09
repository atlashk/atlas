package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.saga.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaMessagePublisher implements SagaMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(SagaCommand command) {
    final String exchange = String.format("saga.%s.command.%s",
            command.getSagaName(), command.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.command.%s",
            command.getSagaName(), command.getTargetServiceName())
        .toLowerCase();
    PublishRequest request = PublishRequest.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .messagePayload(command)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCommandReply reply) {
    final String exchange = String.format("saga.%s.commandreply", reply.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.commandreply", reply.getSagaName())
        .toLowerCase();
    PublishRequest request = PublishRequest.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .messagePayload(reply)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCompensation compensation) {
    final String exchange = String.format("saga.%s.compensation.%s",
            compensation.getSagaName(), compensation.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensation.%s",
            compensation.getSagaName(), compensation.getTargetServiceName())
        .toLowerCase();
    PublishRequest request = PublishRequest.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .messagePayload(compensation)
        .build();
    messagePublisher.publish(request);
  }

  @Override
  public void publish(SagaCompensationReply reply) {
    final String exchange = String.format("saga.%s.compensationreply", reply.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensationreply", reply.getSagaName())
        .toLowerCase();
    PublishRequest request = PublishRequest.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .messagePayload(reply)
        .build();
    messagePublisher.publish(request);
  }
}
