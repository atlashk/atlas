package org.atlas.libs.framework.notification.email;

public interface EmailService {

  void send(SendEmailRequest request) throws SendEmailException;
}
