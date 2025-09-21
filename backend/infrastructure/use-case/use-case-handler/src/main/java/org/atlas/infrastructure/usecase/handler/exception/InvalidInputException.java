package org.atlas.infrastructure.usecase.handler.exception;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class InvalidInputException extends RuntimeException {

  private final List<String> errorMessages;

  public InvalidInputException(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }
}
