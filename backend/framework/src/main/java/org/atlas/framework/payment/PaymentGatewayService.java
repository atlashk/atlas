package org.atlas.framework.payment;

import java.util.Map;
import org.atlas.domain.payment.shared.PaymentGatewayCode;
import org.atlas.framework.payment.exception.PaymentGatewayException;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.payment.model.WebhookResponse;

public interface PaymentGatewayService {

  PaymentGatewayCode supports();

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  WebhookResponse handleWebhook(String rawPayload, Map<String, String> headers)
      throws PaymentGatewayException;
}
