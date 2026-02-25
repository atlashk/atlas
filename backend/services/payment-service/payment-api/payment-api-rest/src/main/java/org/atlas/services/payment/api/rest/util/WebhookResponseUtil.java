package org.atlas.services.payment.api.rest.util;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.payment.model.HandleWebhookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class WebhookResponseUtil {

  public static ResponseEntity<Map<String, Object>> resolve(
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
