package org.atlas.notification.application.service;

import org.atlas.notification.domain.entity.Notification;

public interface NotificationService {

  void create(Notification notification);
}
