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
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "orders")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaOrderEntity extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String code;

  @Column(name = "user_id")
  private Integer userId;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<JpaOrderItemEntity> orderItems;

  private BigDecimal amount;

  @Column(name = "payment_id")
  private Integer paymentId;

  @Column(name = "payment_method")
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Column(name = "cancellation_reason")
  private String cancellationReason;

  public void addOrderItem(JpaOrderItemEntity orderItem) {
    orderItem.setOrder(this);
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItems.add(orderItem);
  }
}
