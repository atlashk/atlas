package org.atlas.infrastructure.persistence.jpa.impl.notification.mapper;

import org.atlas.domain.notification.entity.Notification;
import org.atlas.infrastructure.persistence.jpa.impl.notification.entity.JpaNotification;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaNotificationMapper {

  JpaNotificationMapper INSTANCE = Mappers.getMapper(JpaNotificationMapper.class);

  Notification toNotification(JpaNotification jpaNotification);

  JpaNotification toJpaNotification(Notification notification);
}
