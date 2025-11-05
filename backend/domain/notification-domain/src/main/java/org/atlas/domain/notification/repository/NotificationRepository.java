package org.atlas.domain.notification.repository;

import java.util.List;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.framework.paging.PagingRequest;

public interface NotificationRepository {

  List<Notification> findByUserIdAndChannel(
      Integer userId, NotificationChannel channel, PagingRequest pagingRequest);

  void insert(Notification notification);

  void update(Notification notification);

  void markAsReadAll(Integer userId, NotificationChannel channel);
}
