package org.atlas.framework.realtime.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.realtime.RealtimeEventType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SseEvent<T> {

  private String id;
  private String key;
  private RealtimeEventType type;
  private T payload;
  private long timestamp;
}
