package org.atlas.services.notification.port.in.service;

import org.atlas.services.notification.domain.entity.Notification;

public interface NotificationService {

  void create(Notification notification);
}
