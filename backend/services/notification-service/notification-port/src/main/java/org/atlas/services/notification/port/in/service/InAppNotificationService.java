package org.atlas.services.notification.port.in.service;

import java.util.List;
import org.atlas.services.notification.domain.entity.Notification;

public interface InAppNotificationService {

  List<Notification> retrieveInAppNotification(int limit);

  void markAsReadAll();

  void markAsSucceeded(Notification notification);

  void markAsFailed(Notification notification, String error);
}
