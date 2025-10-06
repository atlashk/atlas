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
public class DomainEvent implements Serializable {

  protected String eventId;
  protected String eventType;
  protected String eventSource;
  protected Long timestamp;
  protected Date processedAt;
  protected Long version;

  public DomainEvent(String eventSource, String eventType) {
    this.eventId = UUIDGenerator.generate();
    this.eventType = eventType;
    this.eventSource = eventSource;
    this.timestamp = Instant.now().toEpochMilli();
    this.version = 0L;
  }
}
