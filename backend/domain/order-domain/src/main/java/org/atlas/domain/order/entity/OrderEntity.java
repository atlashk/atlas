package org.atlas.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer sagaId;
  private String code;
  private UserEntity user;
  private List<OrderItemEntity> orderItems;
  private BigDecimal amount;
  private PaymentEntity payment;
  private OrderStatus status;
  private String cancellationReason;

  public void addOrderItem(OrderItemEntity orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    this.orderItems.add(orderItem);
  }

  public void calculateOrderAmount() {
    this.amount = BigDecimal.ZERO;
    if (CollectionUtil.isEmpty(orderItems)) {
      return;
    }
    for (OrderItemEntity orderItem : orderItems) {
      this.amount = this.amount.add(
          orderItem.getProduct().getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  public enum CancellationReason {

    FAILED_TO_RESERVE_PRODUCT("Failed to reserve product"),
    FAILED_TO_INITIALIZE_PAYMENT("Failed to initialize payment"),
    FAILED_TO_PROCESS_PAYMENT("Failed to process payment"),
    ;

    private final String value;
  }
}
