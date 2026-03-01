package org.atlas.services.payment.port.in.service;

import org.atlas.services.payment.port.in.model.CreatePaymentEventInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentEventInput;

public interface PaymentEventService {

  Integer createPaymentEvent(CreatePaymentEventInput input);

  void updatePaymentEvent(UpdatePaymentEventInput input);
}
