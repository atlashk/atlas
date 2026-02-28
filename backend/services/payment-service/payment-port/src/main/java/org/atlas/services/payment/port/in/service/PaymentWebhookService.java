package org.atlas.services.payment.port.in.service;

import java.util.Map;
import org.atlas.services.payment.port.out.gateway.model.HandleWebhookResponse;

public interface PaymentWebhookService {

  HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers);
}
