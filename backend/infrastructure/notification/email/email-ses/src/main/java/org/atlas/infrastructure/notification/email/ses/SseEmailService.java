package org.atlas.infrastructure.notification.email.ses;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.email.Attachment;
import org.atlas.framework.notification.email.EmailService;
import org.atlas.framework.notification.email.SendEmailException;
import org.atlas.framework.notification.email.SendEmailRequest;
import org.atlas.framework.util.CollectionUtil;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseEmailService implements EmailService {

  private final SesV2Client sesClient;

  @Override
  public void send(SendEmailRequest request) throws SendEmailException {
    try {
      SendEmailResponse response;
      if (CollectionUtil.isNotEmpty(request.getAttachments())) {
        response = sendEmailWithAttachments(request);
      } else {
        response = sendSimpleEmail(request);
      }
      log.info("Sent email successfully: messageId={}, recipients={}", response.messageId(),
          response.messageId());
    } catch (Exception e) {
      log.error("Failed to send email: recipients={}", request.getRecipients(), e);
      throw new SendEmailException(e);
    }
  }

  private SendEmailResponse sendSimpleEmail(SendEmailRequest request) {
    Destination destination = Destination.builder()
        .toAddresses(request.getRecipients())
        .build();

    // Subject
    Content subject = Content.builder()
        .data(request.getSubject())
        .build();

    // Content
    Content content = Content.builder()
        .data(request.getBody())
        .build();

    // Body
    Body body;
    if (request.isHtml()) {
      body = Body.builder()
          .html(content)
          .build();
    } else {
      body = Body.builder()
          .text(content)
          .build();
    }

    Message message = Message.builder()
        .subject(subject)
        .body(body)
        .build();

    EmailContent emailContent = EmailContent.builder()
        .simple(message)
        .build();

    software.amazon.awssdk.services.sesv2.model.SendEmailRequest emailRequest =
        software.amazon.awssdk.services.sesv2.model.SendEmailRequest.builder()
            .destination(destination)
            .content(emailContent)
            .fromEmailAddress(request.getSender())
            .build();

    return sesClient.sendEmail(emailRequest);
  }

  private SendEmailResponse sendEmailWithAttachments(SendEmailRequest request)
      throws MessagingException, IOException {
    MimeMessage mimeMessage = createMimeMessage(request);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    mimeMessage.writeTo(outputStream);
    SdkBytes rawMessageBytes = SdkBytes.fromByteArray(outputStream.toByteArray());
    RawMessage rawMessage = RawMessage.builder()
        .data(rawMessageBytes)
        .build();
    software.amazon.awssdk.services.sesv2.model.SendEmailRequest awsSendEmailRequest =
        software.amazon.awssdk.services.sesv2.model.SendEmailRequest.builder()
            .content(EmailContent.builder().raw(rawMessage).build())
            .build();
    return sesClient.sendEmail(awsSendEmailRequest);
  }

  private MimeMessage createMimeMessage(SendEmailRequest request)
      throws MessagingException, IOException {
    Session session = Session.getDefaultInstance(System.getProperties());
    MimeMessage mimeMessage = new MimeMessage(session);

    mimeMessage.setFrom(new InternetAddress(request.getSender()));
    mimeMessage.setRecipients(RecipientType.TO, String.join(",", request.getRecipients()));
    mimeMessage.setSubject(request.getSubject());

    MimeBodyPart bodyPart = new MimeBodyPart();
    if (request.isHtml()) {
      bodyPart.setContent(request.getBody(), "text/html");
    } else {
      bodyPart.setText(request.getBody());
    }

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(bodyPart);

    for (Attachment attachment : request.getAttachments()) {
      MimeBodyPart attachmentPart = new MimeBodyPart();
      attachmentPart.attachFile(attachment.file());
      attachmentPart.setFileName(attachment.file().getName());
      multipart.addBodyPart(attachmentPart);
    }

    mimeMessage.setContent(multipart);
    return mimeMessage;
  }
}
