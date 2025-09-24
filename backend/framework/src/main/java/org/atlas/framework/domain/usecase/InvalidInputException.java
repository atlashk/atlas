package org.atlas.framework.domain.usecase;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class InvalidInputException extends RuntimeException {

  private final List<String> errorMessages;

  public InvalidInputException(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }
}
