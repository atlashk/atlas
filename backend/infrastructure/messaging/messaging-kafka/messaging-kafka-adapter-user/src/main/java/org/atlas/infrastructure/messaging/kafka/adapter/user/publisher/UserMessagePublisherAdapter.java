package org.atlas.infrastructure.messaging.kafka.adapter.user.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.port.messaging.UserMessagePublisherPort;
import org.atlas.framework.domain.event.contract.user.UserRegisteredEvent;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.core.KafkaProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessagePublisherAdapter implements UserMessagePublisherPort {

  private final MessageGateway messageGateway;
  private final KafkaProps kafkaProps;

  @Override
  public void publish(UserRegisteredEvent event) {
    messageGateway.send(event, String.valueOf(event.getUserId()),
        kafkaProps.getTopic().getUserRegisteredEvent());
  }
}
