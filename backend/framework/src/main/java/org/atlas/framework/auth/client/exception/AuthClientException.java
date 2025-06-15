package org.atlas.framework.auth.client.exception;

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
