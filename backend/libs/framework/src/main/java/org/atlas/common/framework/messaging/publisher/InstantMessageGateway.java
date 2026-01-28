package org.atlas.common.framework.messaging.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.messaging.gateway", havingValue = "instant", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class InstantMessageGateway implements MessageGateway {

  private final MessagePublisher messagePublisher;

  @Override
  public void sendMessage(Message message) {
    messagePublisher.publish(message);
  }
}
