package org.atlas.framework.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.util.UUIDGenerator;

@NoArgsConstructor
@Getter
@Setter
public abstract class DomainEvent implements Serializable {

  private String eventId;
  private String eventSource;
  private Long timestamp;
  private Date processedAt;
  private Long version;

  public DomainEvent(String eventSource) {
    this.eventId = UUIDGenerator.generate();
    this.eventSource = eventSource;
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
