package org.atlas.application.notification.service;

import java.util.List;
import org.atlas.application.notification.model.MarkAsReadAllInput;
import org.atlas.application.notification.model.RetrieveInAppNotificationListInput;
import org.atlas.domain.notification.entity.Notification;

public interface InAppNotificationService {

  String retrieveCurrentInAppServiceType();

  List<Notification> retrieveInAppNotification(RetrieveInAppNotificationListInput input);

  void markAsReadAll(MarkAsReadAllInput input);

  void markAsSucceeded(Notification notification);

  void markAsFailed(Notification notification, String error);
}
