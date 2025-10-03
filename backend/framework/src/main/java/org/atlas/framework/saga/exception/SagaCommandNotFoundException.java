package org.atlas.framework.saga.exception;

public class SagaCommandNotFoundException extends RuntimeException {

  public SagaCommandNotFoundException(String message) {
    super(message);
  }
}
