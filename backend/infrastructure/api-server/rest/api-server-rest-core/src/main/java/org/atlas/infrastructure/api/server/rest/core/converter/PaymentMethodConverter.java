package org.atlas.infrastructure.api.server.rest.core.converter;

import org.atlas.domain.payment.shared.PaymentMethod;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodConverter implements Converter<String, PaymentMethod> {

  @Override
  public PaymentMethod convert(String source) {
    return PaymentMethod.of(source);
  }
}
