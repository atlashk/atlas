package org.atlas.framework.domain.usecase;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class InvalidInputException extends RuntimeException {

  public InvalidInputException(List<String> errorMessages) {
    super(String.join("; ", errorMessages));
  }
}
