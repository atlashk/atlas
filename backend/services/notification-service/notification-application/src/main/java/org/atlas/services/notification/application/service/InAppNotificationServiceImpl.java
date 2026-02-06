package org.atlas.services.notification.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.notification.domain.entity.DeliveryStatus;
import org.atlas.services.notification.domain.entity.Notification;
import org.atlas.services.notification.domain.entity.NotificationChannel;
import org.atlas.services.notification.port.in.model.MarkAsReadAllInput;
import org.atlas.services.notification.port.in.model.RetrieveInAppNotificationListInput;
import org.atlas.services.notification.port.in.service.InAppNotificationService;
import org.atlas.services.notification.port.out.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationServiceImpl implements InAppNotificationService {

  private final NotificationRepository notificationRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Notification> retrieveInAppNotification(RetrieveInAppNotificationListInput input) {
    PagingRequest pagingRequest = PagingRequest.of(0, input.getLimit(), "createdAt",
        PagingRequest.SortOrder.DESC);
    return notificationRepository.findByUserIdAndChannel(
        input.getUserId(), NotificationChannel.IN_APP, pagingRequest);
  }

  @Override
  @Transactional
  public void markAsReadAll(MarkAsReadAllInput input) {
    notificationRepository.markAsReadAll(input.getUserId(), NotificationChannel.IN_APP);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsSucceeded(Notification notification) {
    notification.setDeliveryStatus(DeliveryStatus.SUCCEEDED);
    notificationRepository.update(notification);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsFailed(Notification notification, String error) {
    notification.setDeliveryStatus(DeliveryStatus.FAILED);
    notification.setDeliveryError(error);
    notificationRepository.update(notification);
  }
}