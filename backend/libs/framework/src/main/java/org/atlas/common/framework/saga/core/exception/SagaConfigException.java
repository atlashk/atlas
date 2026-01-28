package org.atlas.common.framework.saga.core.exception;

public class SagaConfigException extends RuntimeException {

  public SagaConfigException(String message) {
    super(message);
  }

  public SagaConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
