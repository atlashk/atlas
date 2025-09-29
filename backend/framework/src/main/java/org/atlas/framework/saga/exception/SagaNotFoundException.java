package org.atlas.framework.saga.exception;

public class SagaNotFoundException extends RuntimeException {

  public SagaNotFoundException(String message) {
    super(message);
  }
}
