package org.atlas.infrastructure.persistence.jpa.adapter.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.entity.PaymentEventStatus;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "payment_event")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaPaymentEvent extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "payment_gateway_id")
  private Integer paymentGatewayId;

  @Column(name = "payment_id")
  private Integer paymentId;

  @Column(name = "payload")
  private String payload;

  @Column(name = "headers")
  private String headers;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private PaymentEventStatus status;

  @Column(name = "error")
  private String error;
}
