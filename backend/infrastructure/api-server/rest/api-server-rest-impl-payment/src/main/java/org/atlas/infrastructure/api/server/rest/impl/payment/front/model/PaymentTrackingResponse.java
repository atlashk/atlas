package org.atlas.infrastructure.api.server.rest.impl.payment.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.paymentgateway.model.nextaction.NextAction;

@Schema(description = "Response object for payment tracking information")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTrackingResponse {

  @Schema(description = "Current status of the payment", example = "CREATED")
  private PaymentStatus status;

  @Schema(description = "Transaction ID associated with the payment", example = "txn_123456789")
  private String transactionId;

  @Schema(description = "Next action required for the payment, if any")
  private NextAction nextAction;

  @Schema(description = "Error code if the payment failed", example = "ERR001")
  private String errorCode;

  @Schema(description = "Error message if the payment failed", example = "Insufficient funds")
  private String errorMessage;

  @Schema(description = "Reason for cancellation if the payment was cancelled", example = "User requested cancellation")
  private String cancellationReason;
}
