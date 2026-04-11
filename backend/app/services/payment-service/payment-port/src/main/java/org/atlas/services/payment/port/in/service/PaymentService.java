package org.atlas.services.payment.port.in.service;

import org.atlas.services.payment.domain.entity.Payment;
import org.atlas.services.payment.port.in.model.CreatePaymentInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;

public interface PaymentService {

  Payment retrievePayment(String id);

  String createPayment(CreatePaymentInput input);

  void updatePayment(UpdatePaymentInput input);

  RetrievePaymentNextActionOutput retrievePaymentNextAction(String orderId);
}
