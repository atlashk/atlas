package org.atlas.infrastructure.messaging.kafka.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
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
  public void publish(SagaCommand message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(String.format("saga.%s.command.%s",
            message.getSagaName(), message.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", message.getSagaId()))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCommandReply message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(String.format("saga.%s.commandreply", message.getSagaName()))
        .routingAttributes(Map.of("messageKey", message.getSagaId()))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensation message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(String.format("saga.%s.compensation.%s",
            message.getSagaName(), message.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", message.getSagaId()))
        .build();
    messagePublisherPort.publish(destination, message);
  }

  @Override
  public void publish(SagaCompensationReply message) {
    MessageDestination destination = MessageDestination.builder()
        .destination(String.format("saga.%s.compensationreply", message.getSagaName()))
        .routingAttributes(Map.of("messageKey", message.getSagaId()))
        .build();
    messagePublisherPort.publish(destination, message);
  }
}
