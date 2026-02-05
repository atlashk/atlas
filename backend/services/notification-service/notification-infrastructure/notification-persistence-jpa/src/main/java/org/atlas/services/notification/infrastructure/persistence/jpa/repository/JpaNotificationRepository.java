package org.atlas.services.notification.infrastructure.persistence.jpa.repository;

import java.util.Date;
import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.notification.domain.entity.NotificationChannel;
import org.atlas.services.notification.infrastructure.persistence.jpa.entity.JpaNotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaBaseRepository<JpaNotificationEntity, Integer> {

  List<JpaNotificationEntity> findByUserIdAndChannel(
      String userId, NotificationChannel notificationChannel, Pageable pageable);

  @Modifying
  @Query("""
        update JpaNotificationEntity n
        set n.readAt = :readAt
        where n.userId = :userId
          and n.channel = :channel
      """)
  void markAsReadAll(
      @Param("userId") String userId,
      @Param("channel") NotificationChannel channel,
      @Param("readAt") Date readAt);
}
