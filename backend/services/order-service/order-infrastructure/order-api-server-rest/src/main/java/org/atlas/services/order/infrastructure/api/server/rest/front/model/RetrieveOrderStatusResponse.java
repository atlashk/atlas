package org.atlas.services.order.infrastructure.api.server.rest.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.order.OrderStatus;

@Schema(description = "Response object containing the status of an order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveOrderStatusResponse {

  @Schema(description = "Current status of the order", example = "AWAITING_PAYMENT")
  private OrderStatus status;

  @Schema(description = "Reason for canceling the order, if applicable", example = "Payment failed")
  private String cancellationReason;
}
