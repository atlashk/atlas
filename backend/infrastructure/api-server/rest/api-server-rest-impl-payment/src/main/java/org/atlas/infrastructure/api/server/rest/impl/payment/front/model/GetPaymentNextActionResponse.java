package org.atlas.infrastructure.api.server.rest.impl.payment.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.payment.model.nextaction.NextAction;

@Schema(description = "Response object for payment next action")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentNextActionResponse {

  @Schema(description = "Next action required for the payment, if any")
  private NextAction nextAction;
}
