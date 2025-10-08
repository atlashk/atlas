package org.atlas.framework.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.util.UUIDGenerator;

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
