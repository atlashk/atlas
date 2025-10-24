package org.atlas.framework.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentResult {

  private Integer paymentId;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
