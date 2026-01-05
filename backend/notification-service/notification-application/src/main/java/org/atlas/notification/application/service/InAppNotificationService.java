package org.atlas.notification.application.service;

import java.util.List;
import org.atlas.notification.application.model.MarkAsReadAllInput;
import org.atlas.notification.application.model.RetrieveInAppNotificationListInput;
import org.atlas.notification.domain.entity.Notification;

public interface InAppNotificationService {

  String retrieveCurrentInAppServiceType();

  List<Notification> retrieveInAppNotification(RetrieveInAppNotificationListInput input);

  void markAsReadAll(MarkAsReadAllInput input);

  void markAsSucceeded(Notification notification);

  void markAsFailed(Notification notification, String error);
}
