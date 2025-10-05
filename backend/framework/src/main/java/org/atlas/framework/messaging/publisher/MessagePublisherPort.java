package org.atlas.framework.messaging.publisher;

public interface MessagePublisherPort {

  void publish(String destination, String messageKey, Object messagePayload);
}
