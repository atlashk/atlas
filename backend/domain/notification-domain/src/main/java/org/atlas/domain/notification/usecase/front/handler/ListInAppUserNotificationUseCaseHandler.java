package org.atlas.domain.notification.usecase.front.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.domain.notification.repository.NotificationRepository;
import org.atlas.domain.notification.usecase.front.model.ListInAppUserNotificationInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingRequest.SortOrder;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class ListInAppUserNotificationUseCaseHandler {

  private final NotificationRepository notificationRepository;

  public List<Notification> handle(ListInAppUserNotificationInput input) throws Exception {
    PagingRequest pagingRequest = PagingRequest.of(0, input.getLimit(), "createdAt",
        SortOrder.DESC);
    return notificationRepository.findByUserIdAndChannel(
        input.getUserId(), NotificationChannel.IN_APP, pagingRequest);
  }
}
