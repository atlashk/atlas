package org.atlas.services.identity.application.spring.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {
    "org.atlas.services.identity.api.rest"
})
@RequiredArgsConstructor
@Slf4j
public class AuthExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponseWrapper<Void> handle(BadCredentialsException e) {
    return ApiResponseWrapper.error(CommonDomainError.UNAUTHORIZED.getErrorCode(),
        "Incorrect email or password");
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponseWrapper<Void> handle(AuthenticationException e) {
    return ApiResponseWrapper.error(CommonDomainError.UNAUTHORIZED.getErrorCode(), e.getMessage());
  }
}
