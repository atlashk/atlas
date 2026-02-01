package org.atlas.services.payment.port.in.front.service;

import org.atlas.services.payment.port.in.front.model.RetrievePaymentNextActionOutput;

public interface PaymentService {

  RetrievePaymentNextActionOutput retrievePaymentNextAction(Integer orderId, Integer userId);
}
