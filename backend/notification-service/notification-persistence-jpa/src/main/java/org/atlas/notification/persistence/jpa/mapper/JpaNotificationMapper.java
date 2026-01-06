package org.atlas.notification.persistence.jpa.mapper;

import org.atlas.notification.domain.entity.Notification;
import org.atlas.notification.persistence.jpa.entity.JpaNotification;
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

  Notification toNotification(JpaNotification jpaNotification);

  JpaNotification toJpaNotification(Notification notification);
}
