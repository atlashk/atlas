package org.atlas.services.user.infrastructure.idp.exception;

public class KeycloakClientException extends RuntimeException {

  public KeycloakClientException(String message) {
    super(message);
  }
}
