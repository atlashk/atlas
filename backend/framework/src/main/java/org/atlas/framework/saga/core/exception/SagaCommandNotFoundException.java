package org.atlas.framework.saga.core.exception;

public class SagaCommandNotFoundException extends RuntimeException {

  public SagaCommandNotFoundException(String message) {
    super(message);
  }
}
