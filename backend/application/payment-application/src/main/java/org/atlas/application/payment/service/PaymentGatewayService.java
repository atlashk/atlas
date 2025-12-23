package org.atlas.application.payment.service;

import java.util.List;
import org.atlas.domain.payment.entity.PaymentGateway;

public interface PaymentGatewayService {

  List<PaymentGateway> retrievePaymentGatewayList();
}
