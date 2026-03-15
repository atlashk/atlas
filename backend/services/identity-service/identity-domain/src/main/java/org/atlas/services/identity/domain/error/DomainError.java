package org.atlas.services.identity.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainError {

  // User-related errors
  USER_NOT_FOUND(1000, "error.user.not_found"),
  USERNAME_ALREADY_EXISTS(1001, "error.user.username_already_exists"),
  EMAIL_ALREADY_EXISTS(1002, "error.user.email_already_exists"),
  PHONE_NUMBER_ALREADY_EXISTS(1003, "error.user.phone_number_already_exists"),
  WRONG_PASSWORD(1004, "error.user.wrong_password"),

  // OAuth2
  OAUTH2_USER_INFO_INVALID(2000, "error.oauth2.user_info_invalid"),
  ;

  private final int errorCode;
  private final String messageCode;

  @Override
  public String toString() {
    return String.format("%d %s", errorCode, name());
  }
}
