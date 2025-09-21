package org.atlas.infrastructure.api.server.rest.impl.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.atlas.domain.order.shared.OrderStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing the status of an order.")
public class GetOrderStatusResponse {

  @Schema(description = "Current status of the order.")
  private OrderStatus status;

  @Schema(description = "Reason for canceling the order, if applicable.")
  private String cancellationReason;
}
