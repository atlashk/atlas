package org.atlas.infrastructure.messaging.rabbitmq.adapter.user.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.UserMessagePublisherPort;
import org.atlas.framework.domain.event.contract.user.UserRegisteredEvent;
import org.atlas.framework.messaging.gateway.MessageGateway;
import org.atlas.infrastructure.messaging.rabbitmq.core.RabbitmqConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessagePublisherAdapter implements UserMessagePublisherPort {

  private final MessageGateway messageGateway;

  @Override
  public void publish(UserRegisteredEvent event) {
    messageGateway.send(event, null, RabbitmqConstant.USER_EVENTS_EXCHANGE);
  }
}
