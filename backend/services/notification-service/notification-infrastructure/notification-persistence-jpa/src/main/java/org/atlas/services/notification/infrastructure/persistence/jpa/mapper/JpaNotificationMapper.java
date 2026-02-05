package org.atlas.services.notification.infrastructure.persistence.jpa.mapper;

import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.infrastructure.persistence.jpa.entity.JpaNotificationEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaNotificationMapper {

  JpaNotificationMapper INSTANCE = Mappers.getMapper(JpaNotificationMapper.class);

  Notification toNotification(JpaNotificationEntity jpaNotification);

  JpaNotificationEntity toJpaNotification(Notification notification);
}
