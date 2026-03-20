package org.atlas.services.order.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.persistence.jpa.converter.StringCryptoConverter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaOrderEntity extends JpaBaseEntity {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "saga_id")
  private Integer sagaId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "user_first_name")
  private String userFirstName;

  @Column(name = "user_last_name")
  private String userLastName;

  @Column(name = "user_email")
  @Convert(converter = StringCryptoConverter.class)
  private String userEmail;

  @Column(name = "user_phone_number")
  @Convert(converter = StringCryptoConverter.class)
  private String userPhoneNumber;

  @Column(name = "address_street")
  private String addressStreet;

  @Column(name = "address_city")
  private String addressCity;

  @Column(name = "address_country")
  private String addressCountry;

  @Column(name = "address_postal_code")
  private String addressPostalCode;

  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      mappedBy = "order",
      orphanRemoval = true
  )
  @Builder.Default
  private List<JpaOrderItemEntity> orderItems = new ArrayList<>();

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "trace_id")
  private String traceId;

  @Column(name = "payment_gateway_id")
  private Integer paymentGatewayId;

  @Column(name = "payment_gateway_name")
  private String paymentGatewayName;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "payment_method_details")
  private String paymentMethodDetails;

  @Column(name = "payment_transaction_id")
  private String paymentTransactionId;

  @Column(name = "cancellation_reason")
  private String cancellationReason;

  public void addOrderItem(JpaOrderItemEntity orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItem.setOrder(this);
    orderItems.add(orderItem);
  }
}
