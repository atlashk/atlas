package org.atlas.application.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.notification.model.MarkAsReadAllInput;
import org.atlas.application.notification.model.RetrieveInAppNotificationListInput;
import org.atlas.application.notification.port.repository.NotificationRepository;
import org.atlas.domain.notification.entity.DeliveryStatus;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.framework.notification.inapp.InAppService;
import org.atlas.framework.paging.PagingRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationServiceImpl implements InAppNotificationService {

  private final ApplicationContext applicationContext;
  private final NotificationRepository notificationRepository;

  @Override
  @Transactional(readOnly = true)
  public String retrieveCurrentInAppServiceType() {
    // Check which InAppService implementation is available in the application context
    String[] beanNames = applicationContext.getBeanNamesForType(InAppService.class);

    if (beanNames.length == 0) {
      log.warn("No InAppService implementation found in application context");
      return "unknown";
    }

    if (beanNames.length > 1) {
      log.warn("Multiple InAppService implementations found: {}. Using the first one.",
          String.join(", ", beanNames));
    }

    String beanName = beanNames[0];
    log.debug("Found InAppService implementation: {}", beanName);

    // Return just the prefix by removing "InAppService" from the bean name
    String serviceType = beanName.replace("InAppService", "");
    log.debug("Extracted service type: {} from bean name: {}", serviceType, beanName);
    return serviceType;
  }

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