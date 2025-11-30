package org.atlas.infrastructure.messaging.kafka.impl.user.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.framework.domain.event.contract.user.UserEvent;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.infrastructure.messaging.kafka.core.constant.KafkaTopics;
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
