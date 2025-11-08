package org.atlas.infrastructure.persistence.jpa.impl.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.notification.entity.DeliveryStatus;
import org.atlas.domain.notification.entity.NotificationChannel;
import org.atlas.domain.notification.entity.NotificationType;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaNotification extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "user_id")
  private Integer userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel")
  private NotificationChannel channel;

  @Column(name = "message")
  private String message;

  @Column(name = "metadata")
  private String metadata;

  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_status")
  private DeliveryStatus deliveryStatus;

  @Column(name = "delivery_error")
  private String deliveryError;

  @Column(name = "read_at")
  private Date readAt;
}
