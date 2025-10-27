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
import org.atlas.domain.payment.shared.PaymentStatus;
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
  private OrderStatus status;
  private UserSnapshot user;
  private Address address;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private PaymentSnapshot payment;
  private String cancellationReason;

  public void addOrderItem(OrderItem orderItem) {
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
    for (OrderItem orderItem : orderItems) {
      BigDecimal itemAmount = orderItem.getProduct().getPrice()
          .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
      this.amount = this.amount.add(itemAmount);
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Address {

    private String street;
    private String city;
    private String country; // Country code
    private String postalCode;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class OrderItem {

    private ProductSnapshot product;
    private Integer quantity;
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

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class UserSnapshot {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductSnapshot {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class PaymentSnapshot {

    private String transactionId;
    private String paymentGateway;
    private String paymentMethod;
    private String paymentMethodDetails;
    private PaymentStatus status;
    private String error;
    private String cancellationReason;
  }
}
