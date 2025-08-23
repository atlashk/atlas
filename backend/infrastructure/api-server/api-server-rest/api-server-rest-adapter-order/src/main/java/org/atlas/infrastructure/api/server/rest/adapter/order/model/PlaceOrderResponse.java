package org.atlas.infrastructure.api.server.rest.adapter.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing the information of placed order.")
public class PlaceOrderResponse {

  @Schema(description = "The identifier of new order.")
  private Integer orderId;

  @Schema(description = "The code of new order.")
  private String orderCode;
}
