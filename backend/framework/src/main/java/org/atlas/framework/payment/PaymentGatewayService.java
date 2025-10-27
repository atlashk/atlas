package org.atlas.framework.payment;

import org.atlas.framework.payment.exception.PaymentGatewayException;
import org.atlas.framework.payment.model.CreatePaymentRequest;
import org.atlas.framework.payment.model.CreatePaymentResponse;
import org.atlas.framework.payment.model.HandleWebhookRequest;
import org.atlas.framework.payment.model.HandleWebhookResponse;

public interface PaymentGatewayService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  HandleWebhookResponse handleWebhook(HandleWebhookRequest request) throws PaymentGatewayException;
}
