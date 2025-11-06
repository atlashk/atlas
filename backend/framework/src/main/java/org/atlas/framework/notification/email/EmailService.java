package org.atlas.framework.notification.email;

public interface EmailService {

  void send(SendEmailRequest request) throws SendEmailException;
}
