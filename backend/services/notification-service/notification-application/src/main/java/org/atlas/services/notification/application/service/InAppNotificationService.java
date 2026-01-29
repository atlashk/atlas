package org.atlas.services.notification.application.service;

import java.util.List;
import org.atlas.services.notification.application.model.MarkAsReadAllInput;
import org.atlas.services.notification.application.model.RetrieveInAppNotificationListInput;
import org.atlas.services.notification.domain.entity.Notification;

public interface InAppNotificationService {

  String retrieveCurrentInAppServiceType();

  List<Notification> retrieveInAppNotification(RetrieveInAppNotificationListInput input);

  void markAsReadAll(MarkAsReadAllInput input);

  void markAsSucceeded(Notification notification);

  void markAsFailed(Notification notification, String error);
}
