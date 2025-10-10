package org.atlas.infrastructure.api.server.rest.impl.order.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object containing the information of placed order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

  @Schema(description = "The identifier of saga")
  private Integer sagaId;
}
