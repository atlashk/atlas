package org.atlas.framework.saga.exception;

public class SagaStepNotFoundException extends RuntimeException {

  public SagaStepNotFoundException(String message) {
    super(message);
  }
}