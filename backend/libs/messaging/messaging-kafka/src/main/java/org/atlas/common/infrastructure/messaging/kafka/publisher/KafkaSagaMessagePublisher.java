package org.atlas.common.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.messaging.publisher.Message;
import org.atlas.common.framework.messaging.publisher.MessagePublisher;
import org.atlas.common.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCompensationReply;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaSagaMessagePublisher implements SagaMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(SagaCommand sagaCommand) {
    Message message = Message.builder()
        .destination(String.format("saga.%s.command.%s",
            sagaCommand.getSagaName(), sagaCommand.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", sagaCommand.getSagaId()))
        .payload(JsonUtil.getInstance().toJson(sagaCommand))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCommandReply sagaCommandReply) {
    Message message = Message.builder()
        .destination(String.format("saga.%s.commandreply", sagaCommandReply.getSagaName()))
        .routingAttributes(Map.of("messageKey", sagaCommandReply.getSagaId()))
        .payload(JsonUtil.getInstance().toJson(sagaCommandReply))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCompensation sagaCompensation) {
    Message message = Message.builder()
        .destination(String.format("saga.%s.compensation.%s",
            sagaCompensation.getSagaName(), sagaCompensation.getTargetServiceName()))
        .routingAttributes(Map.of("messageKey", sagaCompensation.getSagaId()))
        .payload(JsonUtil.getInstance().toJson(sagaCompensation))
        .build();
    messagePublisher.publish(message);
  }

  @Override
  public void publish(SagaCompensationReply sagaCompensationReply) {
    Message message = Message.builder()
        .destination(
            String.format("saga.%s.compensationreply", sagaCompensationReply.getSagaName()))
        .routingAttributes(Map.of("messageKey", sagaCompensationReply.getSagaId()))
        .payload(JsonUtil.getInstance().toJson(sagaCompensationReply))
        .build();
    messagePublisher.publish(message);
  }
}
