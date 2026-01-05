package org.atlas.payment.application.service;

import java.util.Map;
import org.atlas.common.framework.payment.model.HandleWebhookResponse;

public interface PaymentWebhookService {

  HandleWebhookResponse handle(String paymentGatewayCode, String rawPayload,
      Map<String, String> headers);
}
