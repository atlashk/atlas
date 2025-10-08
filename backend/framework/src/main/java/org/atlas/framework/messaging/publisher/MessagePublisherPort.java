package org.atlas.framework.messaging.publisher;

public interface MessagePublisherPort {

  void publish(PublishRequest request);
}
