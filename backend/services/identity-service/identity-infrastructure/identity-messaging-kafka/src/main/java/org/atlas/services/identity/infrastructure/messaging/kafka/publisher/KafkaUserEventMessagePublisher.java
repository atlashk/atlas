package org.atlas.services.identity.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.services.identity.port.out.messaging.UserEventMessagePublisher;
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
