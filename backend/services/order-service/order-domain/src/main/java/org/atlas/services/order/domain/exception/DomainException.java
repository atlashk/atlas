package org.atlas.services.order.domain.exception;

import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.services.order.domain.error.DomainError;

public class DomainException extends BaseDomainException {

  public DomainException(DomainError domainError) {
    super(domainError.getErrorCode(), domainError.getMessageCode());
  }

  public DomainException(CommonDomainError commonDomainError) {
    super(commonDomainError);
  }

  public DomainException(CommonDomainError commonDomainError, String message) {
    super(commonDomainError.getErrorCode(), message);
  }

  public DomainException(DomainError domainError, String message) {
    super(domainError.getErrorCode(), message);
  }

  public DomainException(DomainError domainError, Throwable cause) {
    super(domainError.getErrorCode(), domainError.getMessageCode(), cause);
  }
}
