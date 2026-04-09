package org.atlas.services.user.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@RequiredArgsConstructor
public enum UserDomainError implements DomainError {

  // User-related errors
  USER_NOT_FOUND(1000, "error.user.not_found"),
  USERNAME_ALREADY_EXISTS(1001, "error.user.username_already_exists"),
  EMAIL_ALREADY_EXISTS(1002, "error.user.email_already_exists"),
  PHONE_NUMBER_ALREADY_EXISTS(1003, "error.user.phone_number_already_exists"),
  WRONG_PASSWORD(1004, "error.user.wrong_password"),
  ;

  private final int errorCode;
  private final String messageCode;
}
