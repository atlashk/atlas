package org.atlas.services.order.domain.entity;

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
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private Integer sagaId;

  private OrderStatus status;

  private UserSnapshot user;

  private Address address;

  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();
  
  private BigDecimal amount;
  
  private PaymentSnapshot payment;

  private String cancellationReason;

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
  public static class UserSnapshot {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
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

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductSnapshot {

    private String id;
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

    private Integer paymentGatewayId;
    private String paymentGatewayName;
    private String paymentMethod;
    private String paymentMethodDetails;
    private String transactionId;
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
