package org.atlas.framework.notification.email;

public class SendEmailException extends RuntimeException {

  public SendEmailException(String message) {
    super(message);
  }

  public SendEmailException(Throwable cause) {
    super(cause);
  }
}
