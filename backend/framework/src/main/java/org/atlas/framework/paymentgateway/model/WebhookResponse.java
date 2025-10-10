package org.atlas.framework.paymentgateway.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookResponse {

  private PaymentResult paymentResult;
  private int responseStatus;
  private Map<String, Object> responseBody;
  private Map<String, String> responseHeaders;
}
