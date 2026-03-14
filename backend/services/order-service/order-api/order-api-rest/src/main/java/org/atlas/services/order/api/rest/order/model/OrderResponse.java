package org.atlas.services.order.api.rest.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;

@Schema(description = "Complete order information including items, payment details, and delivery address")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  @Schema(description = "Unique identifier of the order", example = "ORD0000001")
  private String id;

  @Schema(description = "Current status of the order (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)")
  private OrderStatus status;

  @Schema(description = "Delivery address for the order")
  private Address address;

  @Schema(description = "List of products and quantities included in this order")
  private List<OrderItem> orderItems;

  @Schema(description = "Total amount of the order including taxes and fees", example = "149.99")
  private BigDecimal amount;

  @Schema(description = "Payment information and transaction details for the order")
  private Payment payment;

  @Schema(description = "Reason provided when the order was cancelled", example = "Customer requested cancellation")
  private String cancellationReason;

  @Schema(description = "Timestamp when the order was initially created", example = "2024-01-15T10:30:00Z")
  private LocalDateTime createdAt;

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
    private Product product;

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

    @Schema(description = "Unique identifier of the product in the product", example = "789")
    private String id;

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

    @Schema(description = "Payment processing gateway used for the transaction", example = "Stripe")
    private String paymentGatewayName;

    @Schema(description = "Payment method type used by the customer", example = "Credit Card")
    private String paymentMethod;

    @Schema(description = "Additional details about the payment method (e.g., last 4 digits of card)",
        example = "{\"last4\":\"1234\"}")
    private String paymentMethodDetails;

    @Schema(description = "External transaction identifier of the payment", example = "TXN_123456789")
    private String transactionId;
  }
}
