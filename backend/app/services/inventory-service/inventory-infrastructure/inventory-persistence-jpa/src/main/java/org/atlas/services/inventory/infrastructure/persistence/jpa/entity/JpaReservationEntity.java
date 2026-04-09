package org.atlas.services.inventory.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;
import org.atlas.services.inventory.domain.entity.ReservationStatus;

@Entity
@Table(name = "reservation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaReservationEntity extends JpaBaseEntity {

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "product_id")
  private String productId;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ReservationStatus status;
}
