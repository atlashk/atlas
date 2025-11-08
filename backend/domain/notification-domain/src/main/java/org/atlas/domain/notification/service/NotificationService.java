package org.atlas.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.notification.entity.DeliveryStatus;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service is helpful to encapsulate notification writes and call it from async tasks.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void create(Notification notification) {
    notificationRepository.insert(notification);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsSucceeded(Notification notification) {
    notification.setDeliveryStatus(DeliveryStatus.SUCCEEDED);
    notificationRepository.update(notification);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsFailed(Notification notification, String error) {
    notification.setDeliveryStatus(DeliveryStatus.FAILED);
    notification.setDeliveryError(error);
    notificationRepository.update(notification);
  }
}
