package org.atlas.infrastructure.messaging.rabbitmq.impl.user.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.framework.domain.event.contract.user.UserEvent;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqUserEventMessagePublisher implements UserEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(UserEvent event) {
    final String exchange = "user_events";
    final String routingKey = "user_events";
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
