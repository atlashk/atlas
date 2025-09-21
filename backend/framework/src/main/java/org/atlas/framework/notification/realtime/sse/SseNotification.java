package org.atlas.framework.notification.realtime.sse;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.atlas.framework.notification.common.Notification;
import org.atlas.framework.notification.common.NotificationType;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SseNotification<T> extends Notification {

  public SseNotification(NotificationType notificationType, String key, T payload) {
    super(notificationType);
    this.key = key;
    this.payload = payload;
  }

  private String key;
  private T payload;
}
