package org.atlas.framework.notification.realtime.sse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.atlas.framework.notification.common.Notification;
import org.atlas.framework.notification.common.NotificationType;

@Data
@EqualsAndHashCode(callSuper = false)
public class SseNotification<K> extends Notification {

  public SseNotification(NotificationType notificationType, K key, Object payload) {
    super(notificationType);
    this.key = key;
    this.payload = payload;
  }

  private K key;
  private Object payload;
}
