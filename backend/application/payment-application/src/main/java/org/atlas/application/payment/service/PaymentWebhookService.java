package org.atlas.application.payment.service;

import java.util.Map;
import org.atlas.framework.payment.model.HandleWebhookResponse;

public interface PaymentWebhookService {

  HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers);
}
