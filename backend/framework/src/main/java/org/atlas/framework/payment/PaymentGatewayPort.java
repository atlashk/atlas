package org.atlas.framework.payment;

import java.util.Map;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.payment.exception.PaymentGatewayException;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.payment.model.WebhookResponse;

public interface PaymentGatewayPort {

  PaymentGateway supports();

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  WebhookResponse handleWebhook(Map<String, Object> payload, Map<String, String> headers)
      throws PaymentGatewayException;
}
