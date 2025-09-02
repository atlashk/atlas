package org.atlas.infrastructure.messaging.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.messaging.UserMessagePublisherPort;
import org.atlas.framework.messaging.gateway.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.common.KafkaConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessagePublisherAdapter implements UserMessagePublisherPort {

  private final MessageGateway messageGateway;

  @Override
  public void publish(BaseUserEvent event) {
    messageGateway.send(event, String.valueOf(event.getUserId()), KafkaConstant.USER_EVENT_TOPIC);
  }
}
