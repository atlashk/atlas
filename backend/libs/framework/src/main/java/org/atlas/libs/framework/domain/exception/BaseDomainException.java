package org.atlas.libs.framework.domain.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.atlas.libs.framework.domain.error.CommonDomainError;

@Getter
@NoArgsConstructor
public class BaseDomainException extends RuntimeException {

  private int errorCode;

  /**
   * @param message could be plain message or i18n message code
   */
  public BaseDomainException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BaseDomainException(int errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public BaseDomainException(CommonDomainError commonDomainError) {
    this(commonDomainError.getErrorCode(), commonDomainError.getMessageCode());
  }
}
