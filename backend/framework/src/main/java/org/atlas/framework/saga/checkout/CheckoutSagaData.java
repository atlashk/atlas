package org.atlas.framework.saga.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Saga context model
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CheckoutSagaData {

  private Integer orderId;
  private Integer userId;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Integer paymentGatewayId;

  public void addOrderItem(OrderItem orderItem) {
    if (this.orderItems == null) {
      this.orderItems = new ArrayList<>();
    }
    this.orderItems.add(orderItem);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class OrderItem {

    private Integer productId;
    private Integer quantity;
  }
}
