package org.atlas.libs.framework.saga.core.exception;

public class SagaExecutionException extends RuntimeException {

  public SagaExecutionException(String message) {
    super(message);
  }

  public SagaExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
