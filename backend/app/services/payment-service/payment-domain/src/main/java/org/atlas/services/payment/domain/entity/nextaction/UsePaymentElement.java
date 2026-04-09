package org.atlas.services.payment.domain.entity.nextaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The payment gateway should return the client secret. FE will use payment gateway SDK to render
 * payment form and submit.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UsePaymentElement implements NextAction {

  private String publishableKey;
  private String clientSecret;

  @Override
  public NextActionType getType() {
    return NextActionType.USE_PAYMENT_ELEMENT;
  }
}
