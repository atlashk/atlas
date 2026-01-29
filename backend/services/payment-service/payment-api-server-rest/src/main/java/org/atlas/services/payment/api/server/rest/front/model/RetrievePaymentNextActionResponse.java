package org.atlas.services.payment.api.server.rest.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.payment.model.nextaction.NextAction;

@Schema(description = "Response object for payment next action")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievePaymentNextActionResponse {

  @Schema(description = "Next action required for the payment, if any")
  private NextAction nextAction;

  @Schema(description = "Payment amount")
  private BigDecimal amount;

  @Schema(description = "Payment currency")
  private String currency;
}
