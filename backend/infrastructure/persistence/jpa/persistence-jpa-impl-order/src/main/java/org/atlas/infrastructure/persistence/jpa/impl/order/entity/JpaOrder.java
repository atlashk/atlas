package org.atlas.infrastructure.persistence.jpa.impl.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaOrder extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "saga_id")
  private Integer sagaId;

  @Column(name = "code")
  private String code;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "address_street")
  private String addressStreet;

  @Column(name = "address_city")
  private String addressCity;

  @Column(name = "address_country")
  private String addressCountry;

  @Column(name = "address_postal_code")
  private String addressPostalCode;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<JpaOrderItem> orderItems;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "payment_gateway_id")
  private Integer paymentGatewayId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(name = "cancellation_reason")
  private String cancellationReason;
}
