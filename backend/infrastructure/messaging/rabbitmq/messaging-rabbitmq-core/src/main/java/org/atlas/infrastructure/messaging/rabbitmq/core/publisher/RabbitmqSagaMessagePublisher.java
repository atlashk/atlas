package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.core.messaging.payload.SagaCompensationReply;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaMessagePublisher implements SagaMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(SagaCommand sagaCommand) {
    final String exchange = String.format("saga.%s.command.%s",
            sagaCommand.getSagaName(), sagaCommand.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.command.%s",
            sagaCommand.getSagaName(), sagaCommand.getTargetServiceName())
        .toLowerCase();
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(sagaCommand))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCommandReply sagaCommandReply) {
    final String exchange = String.format("saga.%s.commandreply", sagaCommandReply.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.commandreply", sagaCommandReply.getSagaName())
        .toLowerCase();
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(sagaCommandReply))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCompensation sagaCompensation) {
    final String exchange = String.format("saga.%s.compensation.%s",
            sagaCompensation.getSagaName(), sagaCompensation.getTargetServiceName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensation.%s",
            sagaCompensation.getSagaName(), sagaCompensation.getTargetServiceName())
        .toLowerCase();
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(sagaCompensation))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCompensationReply sagaCompensationReply) {
    final String exchange = String.format("saga.%s.compensationreply",
            sagaCompensationReply.getSagaName())
        .toLowerCase();
    final String routingKey = String.format("saga.%s.compensationreply",
            sagaCompensationReply.getSagaName())
        .toLowerCase();
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(sagaCompensationReply))
        .build();
    messagePublisher.publish(message);
  }
}
