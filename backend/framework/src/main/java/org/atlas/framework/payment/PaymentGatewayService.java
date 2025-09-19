package org.atlas.framework.payment;

import java.util.Optional;
import org.atlas.domain.payment.shared.enums.PaymentGateway;

public interface PaymentGatewayService {

  Optional<PaymentGatewayPort> getPaymentGatewayPort(PaymentGateway paymentGateway);
}
