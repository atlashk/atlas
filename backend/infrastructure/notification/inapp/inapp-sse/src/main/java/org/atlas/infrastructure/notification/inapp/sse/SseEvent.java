package org.atlas.infrastructure.notification.inapp.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.notification.inapp.SendInAppRequest.Payload;
import org.atlas.framework.uuid.UUIDGenerator;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SseEvent {

  private String eventId;
  private String eventKey;
  private Payload payload;

  public static SseEvent of(Integer userId, Payload payload) {
    return SseEvent.builder()
        .eventId(UUIDGenerator.generate())
        .eventKey(userId.toString())
        .payload(payload)
        .build();
  }
}
