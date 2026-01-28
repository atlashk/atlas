package org.atlas.payment.application.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.payment.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrievePaymentNextActionOutput {

  private NextAction nextAction;
  private BigDecimal amount;
  private String currency;
}
