package org.atlas.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private String code;
  private UserEntity user;
  private List<OrderItemEntity> orderItems;
  private BigDecimal amount;
  private OrderStatus status;
  private String canceledReason;

  public void addOrderItem(OrderItemEntity orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    this.orderItems.add(orderItem);
  }

  public void calculateOrderAmount() {
    this.amount = BigDecimal.ZERO;
    if (CollectionUtils.isEmpty(orderItems)) {
      return;
    }
    for (OrderItemEntity orderItem : orderItems) {
      this.amount = this.amount.add(
          orderItem.getProduct().getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
    }
  }
}
