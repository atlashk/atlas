package org.atlas.libs.notification.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.Base64Util;
import org.atlas.libs.framework.notification.email.Attachment;
import org.atlas.libs.framework.notification.email.EmailService;
import org.atlas.libs.framework.notification.email.SendEmailException;
import org.atlas.libs.framework.notification.email.SendEmailRequest;
import org.atlas.libs.framework.util.CollectionUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendgridEmailService implements EmailService {

  // Spring Boot automatically configures SendGrid bean
  private final SendGrid sendGrid;

  @Override
  public void send(SendEmailRequest request) throws SendEmailException {
    try {
      Request sendGridRequest = createSendGridRequest(request);
      Response response = sendGrid.api(sendGridRequest);
      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
        log.info("Sent email successfully: recipients={}", request.getRecipients());
      } else {
        log.error("Failed to send email: recipients={}, status={}, body={}",
            request.getRecipients(), response.getStatusCode(), response.getBody());
        throw new SendEmailException("SendGrid API error: " + response.getBody());
      }
    } catch (IOException e) {
      log.error("Failed to send email: recipients={}", request.getRecipients(), e);
      throw new SendEmailException(e);
    }
  }

  private Request createSendGridRequest(SendEmailRequest request) throws IOException {
    Email from = new Email(request.getSender());
    Email[] recipients = request.getRecipients().stream().map(Email::new)
        .toArray(Email[]::new);
    Content content = new Content(request.isHtml() ? "text/html" : "text/plain",
        request.getBody());
    Mail mail = new Mail();
    mail.setFrom(from);
    mail.setSubject(request.getSubject());
    Personalization personalization = new Personalization();
    for (Email recipient : recipients) {
      personalization.addTo(recipient);
    }
    mail.addPersonalization(personalization);
    mail.addContent(content);

    if (CollectionUtil.isNotEmpty(request.getAttachments())) {
      for (Attachment attachment : request.getAttachments()) {
        Attachments sendGridAttachment = new Attachments();
        sendGridAttachment.setFilename(attachment.name());
        byte[] attachmentContent = Files.readAllBytes(attachment.file().toPath());
        sendGridAttachment.setContent(Base64Util.encode(attachmentContent));
        mail.addAttachments(sendGridAttachment);
      }
    }

    Request sendGridRequest = new Request();
    sendGridRequest.setMethod(Method.POST);
    sendGridRequest.setEndpoint("mail/send");
    sendGridRequest.setBody(mail.build());
    return sendGridRequest;
  }
}
