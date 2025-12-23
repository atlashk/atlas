package org.atlas.infrastructure.api.server.rest.adapter.order.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;

@Schema(description = "Represents an order in the order list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  @Schema(description = "Unique identifier of the order", example = "1")
  private Integer id;

  @Schema(description = "Order code", example = "ORD123456")
  private String code;

  @Schema(description = "Current status of the order (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)")
  private OrderStatus status;

  @Schema(description = "User who placed the order")
  private User user;

  @Schema(description = "Delivery address information for the order")
  private Address address;

  @Schema(description = "List of items in the order")
  private List<OrderItem> orderItems;

  @Schema(description = "Total amount of the order", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Payment information for the order")
  private Payment payment;

  @Schema(description = "Reason for canceling the order, if applicable")
  private String cancellationReason;

  @Schema(description = "Date and time when the order was created")
  private Date createdAt;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Represents a user who placed the order")
  public static class User {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Integer id;

    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number of the user", example = "+1-555-123-4567")
    private String phoneNumber;
  }

  @Schema(description = "Delivery address information for the order")
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Address {

    @Schema(description = "Street address including house number and street name", example = "123 Main Street, Apt 4B")
    private String street;

    @Schema(description = "City name for delivery", example = "New York")
    private String city;

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "US")
    private String country;

    @Schema(description = "Postal or ZIP code for the delivery address", example = "10001")
    private String postalCode;
  }

  @Schema(description = "Individual item within an order containing product details and quantity")
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItem {

    @Schema(description = "Product information for this order item")
    private org.atlas.infrastructure.api.server.rest.adapter.order.front.model.OrderResponse.Product product;

    @Schema(description = "Number of units of this product ordered", example = "3", minimum = "1")
    private Integer quantity;
  }

  @Schema(description = "Product information including pricing and visual representation")
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Product {

    @Schema(description = "Unique identifier of the product in the catalog", example = "789")
    private Integer id;

    @Schema(description = "Display name of the product", example = "iPhone 15 Pro Max 256GB")
    private String name;

    @Schema(description = "Unit price of the product in the order currency", example = "1199.99")
    private BigDecimal price;

    @Schema(description = "Product image encoded as Base64 data URL for display purposes",
        example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...")
    private String image;
  }

  @Schema(description = "Payment transaction details and method information")
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Payment {

    @Schema(description = "External transaction identifier of the payment", example = "TXN_123456789")
    private String transactionId;

    @Schema(description = "Payment processing gateway used for the transaction", example = "Stripe")
    private String paymentGateway;

    @Schema(description = "Payment method type used by the customer", example = "Credit Card")
    private String paymentMethod;

    @Schema(description = "Additional details about the payment method (e.g., last 4 digits of card)",
        example = "{\"last4\":\"1234\"}")
    private String paymentMethodDetails;
  }
}
