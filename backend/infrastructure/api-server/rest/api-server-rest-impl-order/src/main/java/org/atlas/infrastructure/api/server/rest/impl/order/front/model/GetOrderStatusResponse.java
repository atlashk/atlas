package org.atlas.infrastructure.api.server.rest.impl.order.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;

@Schema(description = "Response object containing the status of an order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderStatusResponse {

  @Schema(description = "Current status of the order", example = "AWAITING_PAYMENT")
  private OrderStatus status;

  @Schema(description = "Reason for canceling the order, if applicable", example = "Payment failed")
  private String cancellationReason;
}
