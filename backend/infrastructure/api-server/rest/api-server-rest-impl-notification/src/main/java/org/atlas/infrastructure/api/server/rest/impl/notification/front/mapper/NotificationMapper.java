package org.atlas.infrastructure.api.server.rest.impl.notification.front.mapper;

import org.atlas.domain.notification.entity.Notification;
import org.atlas.infrastructure.api.server.rest.impl.notification.front.model.InAppNotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationMapper {

  NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

  InAppNotificationResponse toInAppNotificationResponse(Notification notification);
}
