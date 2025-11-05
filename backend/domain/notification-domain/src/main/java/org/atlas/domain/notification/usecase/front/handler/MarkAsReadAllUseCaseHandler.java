package org.atlas.domain.notification.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.notification.entity.NotificationChannelType;
import org.atlas.domain.notification.repository.NotificationRepository;
import org.atlas.domain.notification.usecase.front.model.MarkAsReadAllInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class MarkAsReadAllUseCaseHandler {

  private final NotificationRepository notificationRepository;

  public void handle(MarkAsReadAllInput input) throws Exception {
    notificationRepository.markAsReadAll(input.getUserId(), NotificationChannelType.IN_APP);
  }
}
