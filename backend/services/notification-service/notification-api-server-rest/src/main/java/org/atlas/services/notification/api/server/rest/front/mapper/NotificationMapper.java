package org.atlas.services.notification.api.server.rest.front.mapper;

import org.atlas.services.notification.api.server.rest.front.model.InAppNotificationResponse;
import org.atlas.services.notification.domain.entity.Notification;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

  NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

  @Mapping(target = "deliveredAt", source = "createdAt")
  InAppNotificationResponse toInAppNotificationResponse(Notification notification);

  @AfterMapping
  default void afterToInAppNotificationResponse(
      @MappingTarget InAppNotificationResponse inAppNotificationResponse,
      Notification notification) {
    if (notification.getReadAt() != null) {
      inAppNotificationResponse.setRead(true);
    }
  }
}
