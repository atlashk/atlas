package org.atlas.infrastructure.persistence.jpa.adapter.notification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.notification.port.repository.NotificationRepository;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.util.DateUtil;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.core.paging.PagingConverter;
import org.atlas.infrastructure.persistence.jpa.adapter.notification.entity.JpaNotification;
import org.atlas.infrastructure.persistence.jpa.adapter.notification.mapper.JpaNotificationMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.notification.repository.JpaNotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

  private final JpaNotificationRepository jpaNotificationRepository;

  @Override
  public List<Notification> findByUserIdAndChannel(
      Integer userId, NotificationChannel channel, PagingRequest pagingRequest) {
    Pageable pageable = PagingConverter.convert(pagingRequest);
    List<JpaNotification> jpaNotifications = jpaNotificationRepository.findByUserIdAndChannel(
        userId, channel, pageable);
    return ObjectMapperUtil.mapList(jpaNotifications,
        JpaNotificationMapper.INSTANCE::toNotification);
  }

  @Override
  public void insert(Notification notification) {
    JpaNotification jpaNotification = JpaNotificationMapper.INSTANCE
        .toJpaNotification(notification);
    jpaNotificationRepository.insert(jpaNotification);
    notification.setId(jpaNotification.getId());
    notification.setCreatedAt(jpaNotification.getCreatedAt());
  }

  @Override
  public void update(Notification notification) {
    JpaNotification jpaNotification = JpaNotificationMapper.INSTANCE
        .toJpaNotification(notification);
    jpaNotificationRepository.save(jpaNotification);
  }

  @Override
  public void markAsReadAll(Integer userId, NotificationChannel channel) {
    jpaNotificationRepository.markAsReadAll(userId, channel, DateUtil.now());
  }
}
