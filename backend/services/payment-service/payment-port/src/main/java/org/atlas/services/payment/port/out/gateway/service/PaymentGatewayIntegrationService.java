package org.atlas.services.payment.port.out.gateway.service;

import org.atlas.services.payment.port.out.gateway.exception.PaymentGatewayException;
import org.atlas.services.payment.port.out.gateway.model.CreatePaymentRequest;
import org.atlas.services.payment.port.out.gateway.model.CreatePaymentResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookRequest;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;

public interface PaymentGatewayIntegrationService {

  CreatePaymentResponse createPayment(CreatePaymentRequest request) throws PaymentGatewayException;

  HandleWebhookResponse handleWebhook(HandleWebhookRequest request) throws PaymentGatewayException;
}
