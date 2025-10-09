package org.atlas.framework.realtime.websocket;

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
public class WebSocketEvent<T> {

  private String id;
  private RealtimeEventType type;
  private T payload;
  private long timestamp;
}
