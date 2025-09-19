package org.atlas.infrastructure.api.server.rest.impl.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.atlas.domain.payment.shared.enums.PaymentGateway;

@Data
@Schema(description = "Request object for placing a new order.")
public class PlaceOrderRequest {

  @NotEmpty(message = "Order items must not be empty.")
  @Schema(description = "List of items to be ordered, must not be empty.", requiredMode = RequiredMode.REQUIRED)
  private List<@Valid OrderItem> orderItems;

  @NotNull
  @Schema(description = "Payment gateway to be used for the order.", example = "STRIPE", requiredMode = RequiredMode.REQUIRED)
  private PaymentGateway paymentGateway;

  @Data
  @Schema(description = "Represents an item in the order.")
  public static class OrderItem {

    @NotNull(message = "Product ID must not be null.")
    @Schema(description = "ID of the product to order.", example = "123", requiredMode = RequiredMode.REQUIRED)
    private Integer productId;

    @NotNull(message = "Quantity must not be null.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    @Schema(description = "Quantity of the product to order, must be at least 1.", example = "2", requiredMode = RequiredMode.REQUIRED)
    private Integer quantity;
  }
}
