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

  private PaymentResult paymentResult;
  private int responseStatus;
  private Map<String, Object> responseBody;
  private Map<String, String> responseHeaders;
}
