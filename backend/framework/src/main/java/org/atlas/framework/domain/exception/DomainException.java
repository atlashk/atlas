package org.atlas.framework.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.error.DomainError;

@Getter
@NoArgsConstructor
public class DomainException extends RuntimeException {

  private int errorCode;
  private String messageCode;

  public DomainException(DomainError error) {
    this.errorCode = error.getErrorCode();
    this.messageCode = error.getMessageCode();
  }

  public DomainException(DomainError error, Throwable cause) {
    super(cause);
    this.errorCode = error.getErrorCode();
    this.messageCode = error.getMessageCode();
  }

  public DomainException(DomainError error, String errorMessage) {
    super(errorMessage);
    this.errorCode = error.getErrorCode();
  }
}
