package org.atlas.framework.messaging.gateway;

public interface MessageGateway {

  void send(Object messagePayload, String messageKey, String destination);
}
