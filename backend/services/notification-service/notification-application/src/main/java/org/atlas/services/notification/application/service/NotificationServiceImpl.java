package org.atlas.services.notification.application.service;

import lombok.RequiredArgsConstructor;
import org.atlas.services.notification.application.port.repository.NotificationRepository;
import org.atlas.services.notification.domain.entity.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void create(Notification notification) {
    notificationRepository.insert(notification);
  }
}
