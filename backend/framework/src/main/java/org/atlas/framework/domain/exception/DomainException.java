package org.atlas.framework.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.error.DomainError;

@Getter
@NoArgsConstructor
public class DomainException extends RuntimeException {

  private int errorCode;

  public DomainException(DomainError error) {
    super(error.getMessageCode());
    this.errorCode = error.getErrorCode();
  }

  public DomainException(DomainError error, Throwable cause) {
    super(error.getMessageCode(), cause);
    this.errorCode = error.getErrorCode();
  }

  public DomainException(DomainError error, String errorMessage) {
    super(errorMessage);
    this.errorCode = error.getErrorCode();
  }
}
