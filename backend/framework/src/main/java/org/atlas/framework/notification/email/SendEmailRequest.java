package org.atlas.framework.notification.email;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.StringUtil;

@Getter
public class SendEmailRequest {

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

    public SendEmailRequest build() {
      if (!validateRequired()) {
        throw new RuntimeException(
            "Failed to build EmailNotification, please check the required fields.");
      }
      SendEmailRequest instance = new SendEmailRequest();
      instance.sender = this.sender;
      instance.recipients = this.recipients;
      instance.subject = this.subject;
      instance.body = this.body;
      instance.attachments = this.attachments;
      instance.html = this.html;
      return instance;
    }

    private boolean validateRequired() {
      return StringUtil.isNotBlank(this.sender) &&
          CollectionUtil.isNotEmpty(recipients) &&
          StringUtil.isNotBlank(subject) &&
          StringUtil.isNotBlank(body);
    }
  }
}
