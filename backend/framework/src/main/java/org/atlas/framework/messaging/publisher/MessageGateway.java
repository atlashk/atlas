package org.atlas.framework.messaging.publisher;

public interface MessageGateway {

  void sendMessage(MessageRequest request);
}
