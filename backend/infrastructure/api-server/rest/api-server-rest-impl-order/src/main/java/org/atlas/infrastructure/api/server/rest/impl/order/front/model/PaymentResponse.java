package org.atlas.infrastructure.api.server.rest.impl.order.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentGatewayCode;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.domain.payment.shared.PaymentStatus;

@Schema(description = "Represents payment information for an order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  @Schema(description = "Unique identifier of the payment", example = "1")
  private Integer id;

  @Schema(description = "External transaction identifier of the payment", example = "TXN_123456789")
  private String transactionId;

  @Schema(description = "Payment amount", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Payment currency", example = "USD")
  private String currency;

  @Schema(description = "Payment method used")
  private PaymentMethod method;

  @Schema(description = "Payment gateway used")
  private PaymentGatewayCode gateway;

  @Schema(description = "Current payment status")
  private PaymentStatus status;

  @Schema(description = "Error code if payment failed")
  private String errorCode;

  @Schema(description = "Error message if payment failed")
  private String errorMessage;

  @Schema(description = "Cancellation reason if payment was canceled")
  private String cancellationReason;
}