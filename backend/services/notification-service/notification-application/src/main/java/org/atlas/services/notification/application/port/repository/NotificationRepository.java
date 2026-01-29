package org.atlas.services.notification.application.port.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.domain.entity.NotificationChannel;

public interface NotificationRepository {

  List<Notification> findByUserIdAndChannel(
      Integer userId, NotificationChannel channel, PagingRequest pagingRequest);

  void insert(Notification notification);

  void update(Notification notification);

  void markAsReadAll(Integer userId, NotificationChannel channel);
}
