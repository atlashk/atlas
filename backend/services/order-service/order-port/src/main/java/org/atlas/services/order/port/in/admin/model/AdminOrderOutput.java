package org.atlas.services.order.port.in.admin.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.domain.payment.PaymentStatus;
import org.atlas.services.order.domain.entity.OrderEntity.OrderItem;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminOrderOutput {

  private Integer id;
  private String code;
  private User user;
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
  public static class Payment {

    private Integer id;
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
