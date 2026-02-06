package org.atlas.services.notification.port.in.service;

import java.util.List;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.port.in.model.MarkAsReadAllInput;
import org.atlas.services.notification.port.in.model.RetrieveInAppNotificationListInput;

public interface InAppNotificationService {

  List<Notification> retrieveInAppNotification(RetrieveInAppNotificationListInput input);

  void markAsReadAll(MarkAsReadAllInput input);

  void markAsSucceeded(Notification notification);

  void markAsFailed(Notification notification, String error);
}
