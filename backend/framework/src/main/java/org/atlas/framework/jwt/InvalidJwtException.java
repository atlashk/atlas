package org.atlas.framework.jwt;

public class InvalidJwtException extends RuntimeException {

  public InvalidJwtException(String message) {
    super(message);
  }

  public InvalidJwtException(Throwable cause) {
    super(cause);
  }

  public InvalidJwtException(String message, Throwable cause) {
    super(message, cause);
  }
}
