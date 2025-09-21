package org.atlas.framework.internalapi.payment;

import java.util.List;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;

public interface PaymentApiPort {

  List<PaymentResponse> call(ListPaymentRequest request);
}
