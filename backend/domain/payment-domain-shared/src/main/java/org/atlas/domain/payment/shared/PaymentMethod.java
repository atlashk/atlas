package org.atlas.domain.payment.shared;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PaymentMethod {

  CARD("card"),
  ;

  private final String type;
}
