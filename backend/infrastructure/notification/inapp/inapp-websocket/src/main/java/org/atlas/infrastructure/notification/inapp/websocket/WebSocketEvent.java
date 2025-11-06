package org.atlas.infrastructure.notification.inapp.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.notification.inapp.SendInAppRequest.Payload;
import org.atlas.framework.util.UUIDGenerator;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WebSocketEvent {

  private String eventId;
  private Payload payload;

  public static WebSocketEvent of(Payload payload) {
    return WebSocketEvent.builder()
        .eventId(UUIDGenerator.generate())
        .payload(payload)
        .build();
  }
}
