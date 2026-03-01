package org.atlas.libs.api.server.rest.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.i18n.I18nService;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order
@RestControllerAdvice(basePackages = {
    "org.atlas.libs.api.server.rest",
    "org.atlas.services"
})
@RequiredArgsConstructor
@Slf4j
public class RestExceptionHandler {

  private final I18nService i18nService;

  @ExceptionHandler(BaseDomainException.class)
  public ResponseEntity<ApiResponseWrapper<Void>> handle(BaseDomainException e) {
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
    String errorMessage = firstFieldError.getDefaultMessage();
    return ApiResponseWrapper.error(CommonDomainError.BAD_REQUEST.getErrorCode(), errorMessage);
  }

  /**
   * Missing a required request parameter
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponseWrapper<Void> handle(MissingServletRequestParameterException e) {
    log.error("Invalid request", e);
    return ApiResponseWrapper.error(CommonDomainError.BAD_REQUEST.getErrorCode(),
        "Missing " + e.getParameterName());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponseWrapper<Void> handle(Exception e) {
    log.error("API error: {}", e.getMessage(), e);
    return ApiResponseWrapper.error(CommonDomainError.DEFAULT.getErrorCode(), e.getMessage());
  }
}
