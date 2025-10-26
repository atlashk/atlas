package org.atlas.domain.order.usecase.front.model;

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
import org.atlas.domain.order.vo.OrderItemVO;
import org.atlas.domain.order.vo.PaymentVO;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderOutput extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer sagaId;
  private String code;
  private Integer userId;
  private List<OrderItemVO> orderItems;
  private BigDecimal amount;
  private PaymentVO payment;
  private OrderStatus status;
  private String cancellationReason;

  public void addOrderItem(OrderItemVO orderItem) {
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
    for (OrderItemVO orderItem : orderItems) {
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
