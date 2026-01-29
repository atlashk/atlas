package org.atlas.libs.framework.notification.email;

public class SendEmailException extends Exception {

  public SendEmailException(String message) {
    super(message);
  }

  public SendEmailException(Throwable cause) {
    super(cause);
  }
}
