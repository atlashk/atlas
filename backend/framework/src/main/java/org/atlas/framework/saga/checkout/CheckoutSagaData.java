package org.atlas.framework.saga.checkout;

import java.math.BigDecimal;
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

  private Integer orderId;
  private String orderCode;
  private User user;
  private Address address;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Integer paymentGatewayId;
  private Date createdAt;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class User {

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

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;
  }
}
