package org.atlas.payment.application.service;

import org.atlas.payment.application.model.RetrievePaymentNextActionOutput;

public interface PaymentService {

  RetrievePaymentNextActionOutput retrievePaymentNextAction(Integer orderId, Integer userId);
}
