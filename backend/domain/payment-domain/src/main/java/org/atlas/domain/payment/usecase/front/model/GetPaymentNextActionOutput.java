package org.atlas.domain.payment.usecase.front.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.payment.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GetPaymentNextActionOutput {

  private NextAction nextAction;
  private BigDecimal amount;
  private String currency;
}
