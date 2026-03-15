package org.atlas.libs.framework.saga.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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

  private String orderId;
  private User user;
  private Address address;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Integer paymentGatewayId;
  private LocalDateTime createdAt;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class User {

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
  public static class OrderItem {

    private Product product;
    private Integer quantity;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Product {

    private String id;
    private String name;
    private BigDecimal price;
    private String image;
  }
}
