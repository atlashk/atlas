package org.atlas.framework.notification.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.framework.util.UUIDGenerator;

@Data
@NoArgsConstructor
public class Notification {

  private String notificationId;
  private NotificationType notificationType;

  public Notification(NotificationType notificationType) {
    this.notificationId = UUIDGenerator.generate();
    this.notificationType = notificationType;
  }
}
