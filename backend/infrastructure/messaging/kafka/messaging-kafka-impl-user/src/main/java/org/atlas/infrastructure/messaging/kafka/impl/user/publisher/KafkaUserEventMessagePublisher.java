package org.atlas.infrastructure.messaging.kafka.impl.user.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventMessagePublisher implements UserEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(BaseUserEvent event) {
    Message message = Message.builder()
        .destination("user_events")
        .routingAttributes(Map.of("messageKey", event.getUser().getId()))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
