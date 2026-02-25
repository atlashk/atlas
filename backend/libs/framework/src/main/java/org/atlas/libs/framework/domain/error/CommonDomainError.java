package org.atlas.libs.framework.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum CommonDomainError {

  DEFAULT(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), "error.commons.default"),
  BAD_REQUEST(HttpStatusCode.BAD_REQUEST.getCode(), "error.commons.bad_request"),
  UNAUTHORIZED(HttpStatusCode.UNAUTHORIZED.getCode(), "error.commons.unauthorized"),
  FORBIDDEN(HttpStatusCode.FORBIDDEN.getCode(), "error.commons.forbidden"),
  NOT_FOUND(HttpStatusCode.NOT_FOUND.getCode(), "error.commons.not_found"),
  CONFLICT(HttpStatusCode.CONFLICT.getCode(), "error.commons.conflict"),
  ;

  private final int errorCode;
  private final String messageCode;

  @Override
  public String toString() {
    return String.format("%d %s", errorCode, name());
  }
}
