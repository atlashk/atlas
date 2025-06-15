package org.atlas.framework.messaging.outbox;

import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OutboxMessageEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long id;
  private String messagePayload;
  private String messageClass;
  private String messageKey;
  private String destination;
  private OutboxMessageStatus status;
  private Date processedAt;
  private String error;
  private Integer retries = 0;

  public void markAsProcessed() {
    this.status = OutboxMessageStatus.PROCESSED;
    this.processedAt = new Date();
  }

  public void incRetries() {
    retries++;
  }

  @Override
  public String toString() {
    return "{" +
        "id=" + id +
        ", payload='" + messagePayload + '\'' +
        '}';
  }
}
