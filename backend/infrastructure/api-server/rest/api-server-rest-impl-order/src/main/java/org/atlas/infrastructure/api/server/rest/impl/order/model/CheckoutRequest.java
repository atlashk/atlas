package org.atlas.infrastructure.api.server.rest.impl.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentMethod;

@Schema(description = "Request object for placing a new order")
@Getter
@Setter
public class CheckoutRequest {

  @NotNull
  @Schema(description = "Payment method to be used for the order", example = "card", requiredMode = RequiredMode.REQUIRED)
  private PaymentMethod paymentMethod;
}
