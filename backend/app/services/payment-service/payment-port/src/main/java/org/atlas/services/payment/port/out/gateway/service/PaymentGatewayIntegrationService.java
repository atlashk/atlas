package org.atlas.services.payment.port.out.gateway.service;

import org.atlas.services.payment.port.out.gateway.exception.PaymentGatewayException;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentRequest;
import org.atlas.services.payment.port.out.gateway.model.CreateExternalPaymentResponse;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookRequest;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;

public interface PaymentGatewayIntegrationService {

  CreateExternalPaymentResponse createPayment(CreateExternalPaymentRequest request) throws PaymentGatewayException;

  HandleWebhookResponse handleWebhook(HandleWebhookRequest request) throws PaymentGatewayException;
}
