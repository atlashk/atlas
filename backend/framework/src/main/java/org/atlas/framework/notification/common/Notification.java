package org.atlas.framework.notification.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.framework.util.UUIDGenerator;

@Data
@NoArgsConstructor
public class Notification {

  private String id;
  private NotificationType type;

  public Notification(NotificationType type) {
    this.id = UUIDGenerator.generate();
    this.type = type;
  }
}
