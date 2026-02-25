package org.atlas.services.payment.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum DomainError {

  DEFAULT(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), "error.commons.default"),
  BAD_REQUEST(HttpStatusCode.BAD_REQUEST.getCode(), "error.commons.bad_request"),
  UNAUTHORIZED(HttpStatusCode.UNAUTHORIZED.getCode(), "error.commons.unauthorized"),
  FORBIDDEN(HttpStatusCode.FORBIDDEN.getCode(), "error.commons.permission_denied"),
  NOT_FOUND(HttpStatusCode.NOT_FOUND.getCode(), "error.commons.not_found"),
  CONFLICT(HttpStatusCode.CONFLICT.getCode(), "error.commons.conflict"),

  // Payment-related errors
  PAYMENT_NOT_FOUND(5000, "error.payment.payment_not_found"),
  INVALID_PAYMENT_STATUS(5001, "error.payment.invalid_status"),
  PAYMENT_GATEWAY_NOT_FOUND(5002, "error.payment.payment_gateway_not_found"),
  ;

  private final int errorCode;
  private final String messageCode;

  @Override
  public String toString() {
    return String.format("%d %s", errorCode, name());
  }
}
