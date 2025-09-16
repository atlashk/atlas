package org.atlas.framework.notification.realtime.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.atlas.framework.notification.common.Notification;
import org.atlas.framework.notification.common.NotificationType;

@Data
@EqualsAndHashCode(callSuper = false)
public class WebSocketNotification extends Notification {

  public WebSocketNotification(NotificationType notificationType, Object payload) {
    super(notificationType);
    this.payload = payload;
  }

  private Object payload;
}
