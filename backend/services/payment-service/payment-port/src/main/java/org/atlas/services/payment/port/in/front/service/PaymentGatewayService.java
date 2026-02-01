package org.atlas.services.payment.port.in.front.service;

import java.util.List;
import org.atlas.services.payment.domain.entity.PaymentGateway;

public interface PaymentGatewayService {

  List<PaymentGateway> retrievePaymentGatewayList();
}
