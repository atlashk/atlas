package org.atlas.infrastructure.messaging.sns.adapter.user.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.port.messaging.UserMessagePublisherPort;
import org.atlas.framework.domain.event.contract.user.UserRegisteredEvent;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessagePublisherAdapter implements UserMessagePublisherPort {

  private final MessageGateway messageGateway;
  private final SnsProps snsProps;

  @Override
  public void publish(UserRegisteredEvent event) {
    messageGateway.send(event, String.valueOf(event.getUserId()),
        snsProps.getSnsTopicArn().getUserRegisteredEvent());
  }
}
