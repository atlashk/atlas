package org.atlas.libs.framework.payment;

import org.atlas.libs.framework.payment.exception.PaymentGatewayException;
import org.atlas.libs.framework.payment.model.CreatePaymentRequest;
import org.atlas.libs.framework.payment.model.CreatePaymentResponse;
import org.atlas.libs.framework.payment.model.HandleWebhookRequest;
import org.atlas.libs.framework.payment.model.HandleWebhookResponse;

public interface PaymentGatewayService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  HandleWebhookResponse handleWebhook(HandleWebhookRequest request) throws PaymentGatewayException;
}
