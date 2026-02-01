package org.atlas.services.payment.port.in.webhook.service;

import java.util.Map;
import org.atlas.libs.framework.payment.model.HandleWebhookResponse;

public interface PaymentWebhookService {

  HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers);
}
