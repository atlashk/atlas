package org.atlas.framework.saga.entity;

public enum SagaCommandStatus {

  STARTED,
  COMPLETED,
  FAILED,
  COMPENSATING,
  COMPENSATED,
  COMPENSATION_FAILED
}
