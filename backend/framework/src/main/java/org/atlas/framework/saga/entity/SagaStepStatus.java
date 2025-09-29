package org.atlas.framework.saga.entity;

public enum SagaStepStatus {

  STARTED,
  COMPLETED,
  FAILED,
  COMPENSATING,
  COMPENSATED,
  COMPENSATION_FAILED;

  public boolean allowsExecution() {
    return this != COMPLETED && this != FAILED;
  }

  public boolean allowsCompensation() {
    return this == FAILED;
  }
}
