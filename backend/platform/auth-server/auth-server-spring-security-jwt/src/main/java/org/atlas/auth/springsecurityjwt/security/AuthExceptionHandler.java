package org.atlas.auth.springsecurityjwt.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.common.framework.domain.common.error.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class AuthExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponseWrapper<Void>> handleBadCredentials(BadCredentialsException e) {
    ApiResponseWrapper<Void> body = ApiResponseWrapper.error(
        DomainError.UNAUTHORIZED.getErrorCode(), "Incorrect username or password");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }
}
