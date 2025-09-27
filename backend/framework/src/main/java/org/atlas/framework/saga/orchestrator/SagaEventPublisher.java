package org.atlas.framework.saga.orchestrator;

public interface SagaEventPublisher {

  void publishStepSuccessEvent(String sagaId, String stepName);

  void publishStepFailureEvent(String sagaId, String stepName, Exception e);

  void publishCompensationEvent(String sagaId, String stepName);

  void publishCompensationFailureEvent(String stepName, Exception e);
}
