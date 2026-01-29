package org.atlas.libs.framework.saga.core.entity;

public enum SagaCommandStatus {

  STARTED,
  COMPLETED,
  FAILED,
  COMPENSATING,
  COMPENSATED,
  COMPENSATION_FAILED
}
