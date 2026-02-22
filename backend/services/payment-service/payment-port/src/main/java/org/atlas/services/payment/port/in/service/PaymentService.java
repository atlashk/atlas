package org.atlas.services.payment.port.in.service;

import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;

public interface PaymentService {

  RetrievePaymentNextActionOutput retrievePaymentNextAction(String orderId);
}
