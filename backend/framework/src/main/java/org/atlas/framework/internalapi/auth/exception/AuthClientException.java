package org.atlas.framework.internalapi.auth.exception;

public class AuthClientException extends RuntimeException {

  public AuthClientException(String message) {
    super(message);
  }

  public AuthClientException(Throwable cause) {
    super(cause);
  }

  public AuthClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
