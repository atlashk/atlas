package org.atlas.framework.messaging;

public interface MessagePublisher {

  void publish(Object messagePayload, String messageKey, String destination);
}
