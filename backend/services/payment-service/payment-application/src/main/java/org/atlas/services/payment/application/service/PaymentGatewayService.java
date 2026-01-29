package org.atlas.services.payment.application.service;

import java.util.List;
import org.atlas.services.payment.domain.entity.PaymentGateway;

public interface PaymentGatewayService {

  List<PaymentGateway> retrievePaymentGatewayList();
}
