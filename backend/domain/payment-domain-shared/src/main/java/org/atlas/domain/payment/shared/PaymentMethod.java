package org.atlas.domain.payment.shared;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PaymentMethod {

  CARD("card"),
  PAYPAL("paypal"),
  ;

  private final String type;

  public static PaymentMethod of(String name) {
    return Arrays.stream(PaymentMethod.values())
        .filter(paymentMethod -> paymentMethod.name().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + name));
  }
}
