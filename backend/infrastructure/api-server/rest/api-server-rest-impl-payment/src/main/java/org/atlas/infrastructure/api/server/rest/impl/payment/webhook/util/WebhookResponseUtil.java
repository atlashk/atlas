package org.atlas.infrastructure.api.server.rest.impl.payment.webhook.util;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.atlas.framework.paymentgateway.model.WebhookResponse;
import org.atlas.framework.util.MapUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class WebhookResponseUtil {

  public static ResponseEntity<Map<String, Object>> convert(WebhookResponse webhookResponse) {
    // Convert status
    HttpStatus status = HttpStatus.resolve(webhookResponse.getResponseStatus());
    if (status == null) {
      throw new IllegalArgumentException(
          "Invalid response status code: " + webhookResponse.getResponseStatus());
    }

    // Convert headers
    HttpHeaders headers = new HttpHeaders();
    if (MapUtil.isNotEmpty(webhookResponse.getResponseHeaders())) {
      webhookResponse.getResponseHeaders().forEach(headers::add);
    }

    // Create ResponseEntity with body, headers, and status
    return new ResponseEntity<>(webhookResponse.getResponseBody(), headers, status);
  }
}
