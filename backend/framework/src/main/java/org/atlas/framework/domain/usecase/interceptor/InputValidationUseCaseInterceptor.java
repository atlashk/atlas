package org.atlas.framework.domain.usecase.interceptor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.usecase.InvalidInputException;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.validation.ValidationService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class InputValidationUseCaseInterceptor implements UseCaseInterceptor {

  private final ValidationService validationService;

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    if (input == null) {
      return;
    }

    List<String> errorMessages = validationService.validate(input);
    if (CollectionUtil.isNotEmpty(errorMessages)) {
      log.error("Validation failed for use case {}: {}",
          useCaseClass.getSimpleName(), errorMessages);
      throw new InvalidInputException(errorMessages);
    }
  }

  @Override
  public void postHandle(Class<?> useCaseClass, Object input) {
    // Ignored
  }

  @Override
  public void onError(Class<?> useCaseClass, Object input, Throwable e) {
    // Ignored
  }
}
