package org.atlas.framework.notification.realtime.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.atlas.framework.notification.common.Notification;
import org.atlas.framework.notification.common.NotificationType;

@Data
@EqualsAndHashCode(callSuper = false)
public class SseNotification extends Notification {

  public SseNotification(NotificationType notificationType, String payload) {
    super(notificationType);
    this.payload = payload;
  }

  private String payload;
}
