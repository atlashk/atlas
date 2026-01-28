package org.atlas.notification.persistence.jpa.repository;

import java.util.Date;
import java.util.List;
import org.atlas.notification.domain.entity.NotificationChannel;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.notification.persistence.jpa.entity.JpaNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaBaseRepository<JpaNotification, Integer> {

  List<JpaNotification> findByUserIdAndChannel(
      Integer userId, NotificationChannel notificationChannel, Pageable pageable);

  @Modifying
  @Query("""
        update JpaNotification n
        set n.readAt = :readAt
        where n.userId = :userId
          and n.channel = :channel
      """)
  void markAsReadAll(
      @Param("userId") Integer userId,
      @Param("channel") NotificationChannel channel,
      @Param("readAt") Date readAt);
}
