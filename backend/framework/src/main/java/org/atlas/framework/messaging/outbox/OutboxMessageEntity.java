package org.atlas.framework.messaging.outbox;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OutboxMessageEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private String message;

  private OutboxMessageStatus status;

  private Date processedAt;

  private String error;

  @Builder.Default
  private Integer retries = 0;

  public void markAsProcessed() {
    this.status = OutboxMessageStatus.PROCESSED;
    this.processedAt = new Date();
  }

  public void incRetries() {
    retries++;
  }
}
