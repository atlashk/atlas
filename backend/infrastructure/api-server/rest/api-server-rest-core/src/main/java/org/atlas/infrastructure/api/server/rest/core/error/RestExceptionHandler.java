package org.atlas.infrastructure.api.server.rest.core.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.i18n.I18nService;
import org.atlas.framework.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {
    "org.atlas.infrastructure.api.server.rest.impl",
    "org.atlas.edge.auth"
})
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandler {

  private final I18nService i18nService;

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ApiResponseWrapper<Void>> handle(DomainException e) {
    // Extract error message
    String i18nMessage = i18nService.getMessage(e.getMessage());
    String errorMessage = StringUtil.isNotBlank(i18nMessage) ? i18nMessage : e.getMessage();

    ApiResponseWrapper<Void> body = ApiResponseWrapper.error(e.getErrorCode(), errorMessage);
    int status = e.getErrorCode() < 1000 ? e.getErrorCode() :
        HttpStatus.INTERNAL_SERVER_ERROR.value();
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Invalid request body
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponseWrapper<Void> handle(MethodArgumentNotValidException e) {
    log.error("Invalid request", e);
    FieldError firstFieldError = e.getBindingResult().getFieldErrors().get(0);
    String message = String.format("[%s] %s", firstFieldError.getField(),
        firstFieldError.getDefaultMessage());
    return ApiResponseWrapper.error(DomainError.BAD_REQUEST.getErrorCode(), message);
  }

  /**
   * Missing a required request parameter
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponseWrapper<Void> handle(MissingServletRequestParameterException e) {
    log.error("Invalid request", e);
    return ApiResponseWrapper.error(DomainError.BAD_REQUEST.getErrorCode(),
        "Missing " + e.getParameterName());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponseWrapper<Void> handle(Exception e) {
    return ApiResponseWrapper.error(DomainError.DEFAULT.getErrorCode(), e.getMessage());
  }
}
