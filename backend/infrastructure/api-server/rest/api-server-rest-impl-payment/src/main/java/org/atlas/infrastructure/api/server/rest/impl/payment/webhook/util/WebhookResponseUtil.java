package org.atlas.infrastructure.api.server.rest.impl.payment.webhook.util;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.atlas.framework.payment.model.HandleWebhookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class WebhookResponseUtil {

  public static ResponseEntity<Map<String, Object>> convert(
      HandleWebhookResponse handleWebhookResponse) {
    // Convert status
    HttpStatus status = HttpStatus.resolve(handleWebhookResponse.getResponseStatus());
    if (status == null) {
      throw new IllegalArgumentException(
          "Invalid response status code: " + handleWebhookResponse.getResponseStatus());
    }

    // Create ResponseEntity with body, headers, and status
    return new ResponseEntity<>(handleWebhookResponse.getResponseBody(), status);
  }
}
