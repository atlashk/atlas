package org.atlas.common.framework.domain.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.uuid.UUIDGenerator;

@NoArgsConstructor
@Getter
@Setter
public class DomainEvent implements Serializable {

  private String eventId;
  private DomainEventType eventType;
  private Long timestamp;
  private Date processedAt;
  private Long version;

  public DomainEvent(DomainEventType eventType) {
    this.eventId = UUIDGenerator.generate();
    this.eventType = eventType;
    this.timestamp = Instant.now().toEpochMilli();
    this.version = 0L;
  }

  public void markAsProcessed() {
    this.processedAt = new Date();
  }

  public boolean isProcessed() {
    return processedAt != null;
  }

  public void incrementVersion() {
    this.version++;
  }
}
