package org.atlas.libs.framework.saga.core.exception;

public class SagaConfigException extends RuntimeException {

  public SagaConfigException(String message) {
    super(message);
  }

  public SagaConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
