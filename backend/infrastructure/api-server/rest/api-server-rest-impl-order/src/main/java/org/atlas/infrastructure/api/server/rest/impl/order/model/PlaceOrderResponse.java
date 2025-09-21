package org.atlas.infrastructure.api.server.rest.impl.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Schema(description = "Response object containing the information of placed order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {

  @Schema(description = "The identifier of new order")
  private Integer orderId;

  @Schema(description = "The code of new order")
  private String orderCode;
}
