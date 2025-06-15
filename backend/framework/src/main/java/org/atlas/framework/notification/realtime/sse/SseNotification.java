package org.atlas.framework.notification.realtime.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SseNotification<K> {

  private String notificationId;
  private RealtimeNotificationType type;
  private K key;
  private Object payload;
}
