package org.atlas.framework.paymentgateway.model.nextaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum NextActionType {

  REDIRECT_URL("redirect_url"),
  DEEPLINK("deeplink"),
  QR_CODE("qr_code"),
  USE_PAYMENT_ELEMENT("use_payment_element");

  private final String type;
}
