package org.atlas.infrastructure.api.server.rest.adapter.order.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.order.shared.enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an order in the order list.")
public class OrderResponse {

  @Schema(description = "Unique identifier of the order", example = "1")
  private Integer id;

  @Schema(description = "Order code", example = "ORD123456")
  private String code;

  @Schema(description = "User who placed the order")
  private UserResponse user;

  @Schema(description = "List of items in the order")
  private List<OrderItemResponse> orderItems;

  @Schema(description = "Total amount of the order", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Current status of the order")
  private OrderStatus status;

  @Schema(description = "Reason for canceling the order, if applicable")
  private String cancelReason;

  @Schema(description = "Date and time when the order was created")
  private Date createdAt;
}
