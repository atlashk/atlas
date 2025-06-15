package org.atlas.framework.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class InstantMessageGateway implements MessageGateway {

  private final MessagePublisher messagePublisher;

  @Override
  public void send(Object messagePayload, String messageKey, String destination) {
    // Publish immediately
    messagePublisher.publish(messagePayload, messageKey, destination);
  }
}
