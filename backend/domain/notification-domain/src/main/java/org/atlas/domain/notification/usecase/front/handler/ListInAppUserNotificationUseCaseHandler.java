package org.atlas.domain.notification.usecase.front.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannelType;
import org.atlas.domain.notification.repository.NotificationRepository;
import org.atlas.domain.notification.usecase.front.model.ListInAppUserNotificationInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class ListInAppUserNotificationUseCaseHandler {

  private final NotificationRepository notificationRepository;

  public List<Notification> handle(ListInAppUserNotificationInput input) throws Exception {
    return notificationRepository.findByUserIdAndNotificationChannelType(
        input.getUserId(), NotificationChannelType.IN_APP, input.getPagingRequest());
  }
}
