package org.atlas.framework.domain.usecase.interceptor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.usecase.InvalidInputException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class InputValidationUseCaseInterceptor implements UseCaseInterceptor {

  private final Validator validator;

  @Override
  public void preHandle(Class<?> useCaseClass, Object input) {
    Set<ConstraintViolation<Object>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      List<String> errorMessages = violations.stream()
          .map(ConstraintViolation::getMessage)
          .toList();
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
