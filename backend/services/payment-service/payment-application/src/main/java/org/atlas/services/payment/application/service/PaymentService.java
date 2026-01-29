package org.atlas.services.payment.application.service;

import org.atlas.services.payment.application.model.RetrievePaymentNextActionOutput;

public interface PaymentService {

  RetrievePaymentNextActionOutput retrievePaymentNextAction(Integer orderId, Integer userId);
}
