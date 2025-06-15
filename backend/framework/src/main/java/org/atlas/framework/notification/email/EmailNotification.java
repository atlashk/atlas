package org.atlas.framework.notification.email;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailNotification {

  private String notificationId;
  private String sender;
  private List<String> recipients;
  private String subject;
  private String body;
  private List<Attachment> attachments;
  private boolean html;

  public static class Builder {

    private String sender;
    private List<String> recipients;
    private String subject;
    private String body;
    private List<Attachment> attachments;
    private boolean html;

    public Builder setSender(String sender) {
      this.sender = sender;
      return this;
    }

    public Builder setRecipients(List<String> recipients) {
      this.recipients = recipients;
      return this;
    }

    public Builder addRecipient(String recipient) {
      if (recipients == null) {
        recipients = new ArrayList<>();
      }
      this.recipients.add(recipient);
      return this;
    }

    public Builder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder setBody(String body) {
      this.body = body;
      return this;
    }

    public Builder setAttachments(List<Attachment> attachments) {
      this.attachments = attachments;
      return this;
    }

    public Builder addAttachment(Attachment attachment) {
      if (attachments == null) {
        attachments = new ArrayList<>();
      }
      this.attachments.add(attachment);
      return this;
    }

    public Builder setHtml(boolean html) {
      this.html = html;
      return this;
    }

    public EmailNotification build() {
      if (!validateRequired()) {
        throw new RuntimeException(
            "Failed to build SendMailRequest, please check the required fields.");
      }
      EmailNotification request = new EmailNotification();
      request.sender = this.sender;
      request.recipients = this.recipients;
      request.subject = this.subject;
      request.body = this.body;
      request.attachments = this.attachments;
      request.html = this.html;
      return request;
    }

    private boolean validateRequired() {
      return StringUtils.isNotBlank(this.sender) &&
          CollectionUtils.isNotEmpty(recipients) &&
          StringUtils.isNotBlank(subject) &&
          StringUtils.isNotBlank(body);
    }
  }
}
