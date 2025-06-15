package org.atlas.framework.messaging;

public interface MessageGateway {

  void send(Object messagePayload, String messageKey, String destination);
}
