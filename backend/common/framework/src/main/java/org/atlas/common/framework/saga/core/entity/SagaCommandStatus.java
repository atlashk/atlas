package org.atlas.common.framework.saga.core.entity;

public enum SagaCommandStatus {

  STARTED,
  COMPLETED,
  FAILED,
  COMPENSATING,
  COMPENSATED,
  COMPENSATION_FAILED
}
