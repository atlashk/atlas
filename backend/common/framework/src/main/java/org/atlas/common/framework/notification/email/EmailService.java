package org.atlas.common.framework.notification.email;

public interface EmailService {

  void send(SendEmailRequest request) throws SendEmailException;
}
