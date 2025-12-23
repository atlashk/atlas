package org.atlas.application.payment.service;

import org.atlas.application.payment.model.RetrievePaymentNextActionOutput;

public interface PaymentService {

  RetrievePaymentNextActionOutput retrievePaymentNextAction(Integer orderId, Integer userId);
}
