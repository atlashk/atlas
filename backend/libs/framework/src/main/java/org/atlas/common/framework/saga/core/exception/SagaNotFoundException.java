package org.atlas.common.framework.saga.core.exception;

public class SagaNotFoundException extends RuntimeException {

  public SagaNotFoundException(String message) {
    super(message);
  }
}
