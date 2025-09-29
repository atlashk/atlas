package org.atlas.framework.saga.event;

public interface SagaEventPublisher {

  // ========== Step Events ==========

  void publish(StepExecutionRequest request);

  void publish(StepExecutionReply reply);

  // ========== Compensation Events ==========

  void publish(StepCompensationRequest request);

  void publish(StepCompensationReply reply);
}
