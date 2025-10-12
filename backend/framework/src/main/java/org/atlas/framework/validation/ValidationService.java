package org.atlas.framework.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationService {

  private final Validator validator;

  public List<String> validate(Object input) {
    Set<ConstraintViolation<Object>> violations = validator.validate(input);
    return violations.stream()
        .map(this::formatViolationMessage)
        .filter(StringUtil::isNotBlank)
        .toList();
  }

  private String formatViolationMessage(ConstraintViolation<Object> violation) {
    String message = violation.getMessage();
    if (StringUtil.isBlank(message)) {
      return null;
    }

    String fieldPath = violation.getPropertyPath().toString();
    if (StringUtil.isBlank(fieldPath)) {
      return message;
    }

    return String.format("[%s] %s", fieldPath, message);
  }
}
