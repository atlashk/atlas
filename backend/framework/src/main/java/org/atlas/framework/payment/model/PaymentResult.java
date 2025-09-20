package org.atlas.framework.payment.model;

import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;

@Getter
@Setter
public class PaymentResult {

  private Integer paymentId;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
