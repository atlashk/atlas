package org.atlas.framework.payment.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WebhookResponse {

  // Respond to the external payment gateway
  private int responseStatus;
  private Map<String, Object> responseBody;
  private Map<String, String> responseHeaders;

  // Respond to order-service (saga command reply)
  private PaymentResult paymentResult;
}
