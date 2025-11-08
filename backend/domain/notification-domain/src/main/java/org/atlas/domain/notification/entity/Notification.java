package org.atlas.domain.notification.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Notification extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer userId;
  private NotificationType type;
  private NotificationChannel channel;
  private String message;
  private String metadata;
  private DeliveryStatus deliveryStatus;
  private String deliveryError;
  private Date readAt;
}
