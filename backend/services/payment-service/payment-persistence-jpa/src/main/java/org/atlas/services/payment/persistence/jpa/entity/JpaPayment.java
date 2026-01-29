package org.atlas.services.payment.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.payment.PaymentStatus;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaPayment extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "order_id")
  private Integer orderId;

  @Column(name = "saga_id")
  private Integer sagaId;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "currency")
  private String currency;

  @Column(name = "payment_gateway_id")
  private Integer paymentGatewayId;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "payment_method_details")
  private String paymentMethodDetails;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private PaymentStatus status;

  // External payment gateway information
  @Column(name = "transaction_id")
  private String transactionId;

  @Column(name = "next_action")
  private String nextAction;

  @Column(name = "error")
  private String error;

  @Column(name = "cancellation_reason")
  private String cancellationReason;
}
