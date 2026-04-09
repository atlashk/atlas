package org.atlas.services.payment.port.in.service;

import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.port.in.model.CreatePaymentInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;

public interface PaymentService {

  PaymentEntity retrievePayment(String id);

  String createPayment(CreatePaymentInput input);

  void updatePayment(UpdatePaymentInput input);

  RetrievePaymentNextActionOutput retrievePaymentNextAction(String orderId);
}
