package org.atlas.infrastructure.messaging.sns.impl.user.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnsUserEventMessagePublisher implements UserEventMessagePublisher {

  private final SnsProps snsProps;
  private final MessagePublisher messagePublisher;

  @Override
  public void publish(BaseUserEvent event) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn().get("user_events"))
        .messagePayload(event)
        .build();
    messagePublisher.publish(request);
  }
}
