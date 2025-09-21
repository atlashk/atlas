package org.atlas.framework.notification.realtime.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.atlas.framework.notification.common.Notification;
import org.atlas.framework.notification.common.NotificationType;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class WebSocketNotification<T> extends Notification {

  public WebSocketNotification(NotificationType notificationType, T payload) {
    super(notificationType);
    this.payload = payload;
  }

  private T payload;
}
