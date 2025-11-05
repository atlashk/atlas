package org.atlas.domain.notification.repository;

import java.util.List;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannelType;
import org.atlas.framework.paging.PagingRequest;

public interface NotificationRepository {

  List<Notification> findByUserIdAndNotificationChannelType(Integer userId,
      NotificationChannelType notificationChannelType, PagingRequest pagingRequest);

  void insert(Notification notification);

  void update(Notification notification);

  void markAsReadAll(Integer userId, NotificationChannelType notificationChannelType);
}
