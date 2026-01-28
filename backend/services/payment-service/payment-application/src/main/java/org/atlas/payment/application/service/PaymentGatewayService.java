package org.atlas.payment.application.service;

import java.util.List;
import org.atlas.payment.domain.entity.PaymentGateway;

public interface PaymentGatewayService {

  List<PaymentGateway> retrievePaymentGatewayList();
}
