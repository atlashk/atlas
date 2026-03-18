package org.atlas.libs.framework.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@NoArgsConstructor
public class DomainException extends RuntimeException {

  private int errorCode;

  /**
   * @param message could be plain message or i18n message code
   */
  public DomainException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public DomainException(int errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public DomainException(DomainError domainError) {
    this(domainError.getErrorCode(), domainError.getMessageCode());
  }

  public DomainException(DomainError domainError, String message) {
    this(domainError.getErrorCode(), message);
  }
}
