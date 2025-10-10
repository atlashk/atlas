package org.atlas.framework.paymentgateway.model.nextaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The payment gateway should return the client secret. FE will use payment gateway SDK to render
 * payment form and submit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsePaymentElement implements NextAction {

  private String provider;
  private String clientSecret;
  private String publishableKey;

  @Override
  public NextActionType getType() {
    return NextActionType.USE_PAYMENT_ELEMENT;
  }
}
