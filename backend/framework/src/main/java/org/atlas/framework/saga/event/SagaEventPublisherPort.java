package org.atlas.framework.saga.event;

public interface SagaEventPublisherPort {

  void publish(SagaEvent event);
}
