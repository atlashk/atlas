package org.atlas.framework.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.atlas.framework.error.AppError;

@Getter
@NoArgsConstructor
public class DomainException extends RuntimeException {

  private int errorCode;
  private String messageCode;

  public DomainException(AppError error) {
    this.errorCode = error.getErrorCode();
    this.messageCode = error.getMessageCode();
  }

  public DomainException(AppError error, String errorMessage) {
    super(errorMessage);
    this.errorCode = error.getErrorCode();
  }
}
