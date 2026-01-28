package org.atlas.notification.application.port.repository;

import java.util.List;
import org.atlas.notification.domain.entity.Notification;
import org.atlas.notification.domain.entity.NotificationChannel;
import org.atlas.common.framework.paging.PagingRequest;

public interface NotificationRepository {

  List<Notification> findByUserIdAndChannel(
      Integer userId, NotificationChannel channel, PagingRequest pagingRequest);

  void insert(Notification notification);

  void update(Notification notification);

  void markAsReadAll(Integer userId, NotificationChannel channel);
}
