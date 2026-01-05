package org.atlas.common.framework.saga.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InitializePaymentCommandMetadata {

  private String transactionId;
  private String paymentGatewayName;
}
