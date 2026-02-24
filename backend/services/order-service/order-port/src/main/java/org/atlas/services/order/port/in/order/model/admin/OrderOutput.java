package org.atlas.services.order.port.in.order.model.admin;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderOutput {

  private String id;
  private User user;
  private Address address;
  private List<OrderItem> orderItems;
  private BigDecimal amount;
  private Payment payment;
  private OrderStatus status;
  private String cancellationReason;

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
    private String phoneNumber;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
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
  public static class Payment {

    private String id;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String paymentGateway;
    private String paymentMethod;
    private String paymentMethodDetails;
    private PaymentStatus status;
    private String errorCode;
    private String errorMessage;
    private String cancellationReason;
  }
}
