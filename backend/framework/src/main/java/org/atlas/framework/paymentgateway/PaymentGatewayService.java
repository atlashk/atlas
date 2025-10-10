package org.atlas.framework.paymentgateway;

import java.util.Map;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.framework.paymentgateway.exception.PaymentGatewayException;
import org.atlas.framework.paymentgateway.model.CreatePaymentRequest;
import org.atlas.framework.paymentgateway.model.CreatePaymentResponse;
import org.atlas.framework.paymentgateway.model.WebhookResponse;

public interface PaymentGatewayService {

  PaymentGateway supports();

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  WebhookResponse handleWebhook(Map<String, Object> payload, Map<String, String> headers)
      throws PaymentGatewayException;
}
