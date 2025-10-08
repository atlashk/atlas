package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.saga.messaging.SagaMessagePublisherPort;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaMessagePublisherAdapter implements SagaMessagePublisherPort {

  private final MessagePublisherPort messagePublisherPort;

  @Override
  public void publish(SagaCommand message) {
    final String exchange = String.format("saga.%s.command.%s",
            message.getSagaName(), message.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.command.%s",
            message.getSagaName(), message.getTargetServiceName())
        .toLowerCase();
    MessageDestination destination = MessageDestination.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCommandReply message) {
    final String exchange = String.format("saga.%s.commandreply", message.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.commandreply", message.getSagaName())
        .toLowerCase();
    MessageDestination destination = MessageDestination.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensation message) {
    final String exchange = String.format("saga.%s.compensation.%s",
            message.getSagaName(), message.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensation.%s",
            message.getSagaName(), message.getTargetServiceName())
        .toLowerCase();
    MessageDestination destination = MessageDestination.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensationReply message) {
    final String exchange = String.format("saga.%s.compensationreply", message.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensationreply", message.getSagaName())
        .toLowerCase();
    MessageDestination destination = MessageDestination.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .build();
    messagePublisherPort.publish(destination, message);
  }
}
