package org.atlas.services.notification.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.persistence.jpa.paging.PagingConverter;
import org.atlas.services.notification.port.out.repository.NotificationRepository;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.domain.entity.NotificationChannel;
import org.atlas.services.notification.infrastructure.persistence.jpa.entity.JpaNotificationEntity;
import org.atlas.services.notification.infrastructure.persistence.jpa.mapper.JpaNotificationMapper;
import org.atlas.services.notification.infrastructure.persistence.jpa.repository.JpaNotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

  private final JpaNotificationRepository jpaNotificationRepository;

  @Override
  public List<Notification> findByUserIdAndChannel(
      String userId, NotificationChannel channel, PagingRequest pagingRequest) {
    Pageable pageable = PagingConverter.convert(pagingRequest);
    List<JpaNotificationEntity> jpaNotifications = jpaNotificationRepository.findByUserIdAndChannel(
        userId, channel, pageable);
    return MapperUtil.mapList(jpaNotifications,
        JpaNotificationMapper.INSTANCE::toNotification);
  }

  @Override
  public void insert(Notification notification) {
    JpaNotificationEntity jpaNotification = JpaNotificationMapper.INSTANCE
        .toJpaNotification(notification);
    jpaNotificationRepository.insert(jpaNotification);
    notification.setId(jpaNotification.getId());
    notification.setCreatedAt(jpaNotification.getCreatedAt());
  }

  @Override
  public void update(Notification notification) {
    JpaNotificationEntity jpaNotification = JpaNotificationMapper.INSTANCE
        .toJpaNotification(notification);
    jpaNotificationRepository.save(jpaNotification);
  }

  @Override
  public void markAsReadAll(String userId, NotificationChannel channel) {
    jpaNotificationRepository.markAsReadAll(userId, channel, DateUtil.now());
  }
}
