package org.atlas.libs.framework.messaging.outbox;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;

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

  private LocalDateTime processedAt;

  private String error;

  @Builder.Default
  private Integer retries = 0;

  public void markAsProcessed() {
    this.status = OutboxMessageStatus.PROCESSED;
    this.processedAt = LocalDateTime.now();
  }

  public void incRetries() {
    retries++;
  }
}
