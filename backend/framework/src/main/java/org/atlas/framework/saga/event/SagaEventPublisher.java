package org.atlas.framework.saga.event;

public interface SagaEventPublisher {

  // ========== Step Events ==========

  void publish(SagaCommandEvent event);

  void publish(SagaCommandReplyEvent event);

  // ========== Compensation Events ==========
}
