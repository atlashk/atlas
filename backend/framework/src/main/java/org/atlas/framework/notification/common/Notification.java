package org.atlas.framework.notification.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.util.UUIDGenerator;

@Getter
@Setter
@NoArgsConstructor
public class Notification {

  private String id;
  private NotificationType type;

  public Notification(NotificationType type) {
    this.id = UUIDGenerator.generate();
    this.type = type;
  }
}
