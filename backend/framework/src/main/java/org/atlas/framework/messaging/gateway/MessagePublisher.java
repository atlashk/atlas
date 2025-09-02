package org.atlas.framework.messaging.gateway;

public interface MessagePublisher {

  void publish(Object messagePayload, String messageKey, String destination);
}
