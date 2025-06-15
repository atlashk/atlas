package org.atlas.framework.notification.realtime.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.notification.realtime.enums.RealtimeNotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WebSocketNotification {

  private String notificationId;
  private RealtimeNotificationType type;
  private Object payload;
}
