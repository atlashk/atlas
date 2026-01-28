package org.atlas.user.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.user.application.port.messaging.UserEventMessagePublisher;
import org.atlas.common.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.messaging.publisher.Message;
import org.atlas.common.framework.messaging.publisher.MessagePublisher;
import org.atlas.common.infrastructure.messaging.kafka.common.KafkaTopics;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventMessagePublisher implements UserEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(UserEvent event) {
    Message message = Message.builder()
        .destination(KafkaTopics.USER_EVENTS)
        .routingAttributes(Map.of("messageKey", event.getUserId()))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
