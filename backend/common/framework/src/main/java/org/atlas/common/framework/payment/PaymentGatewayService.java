package org.atlas.common.framework.payment;

import org.atlas.common.framework.payment.exception.PaymentGatewayException;
import org.atlas.common.framework.payment.model.CreatePaymentRequest;
import org.atlas.common.framework.payment.model.CreatePaymentResponse;
import org.atlas.common.framework.payment.model.HandleWebhookRequest;
import org.atlas.common.framework.payment.model.HandleWebhookResponse;

public interface PaymentGatewayService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  HandleWebhookResponse handleWebhook(HandleWebhookRequest request) throws PaymentGatewayException;
}
