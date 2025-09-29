package org.atlas.framework.saga.entity;

public enum SagaStatus {

  STARTED,
  COMPLETED,
  FAILED;

  /**
   * Check if this status allows step execution
   */
  public boolean allowsStepExecution() {
    return this != COMPLETED && this != FAILED;
  }
}
