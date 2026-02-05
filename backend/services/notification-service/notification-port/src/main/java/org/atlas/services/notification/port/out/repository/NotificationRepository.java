package org.atlas.services.notification.port.out.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.domain.entity.NotificationChannel;

public interface NotificationRepository {

  List<Notification> findByUserIdAndChannel(
      String userId, NotificationChannel channel, PagingRequest pagingRequest);

  void insert(Notification notification);

  void update(Notification notification);

  void markAsReadAll(String userId, NotificationChannel channel);
}
