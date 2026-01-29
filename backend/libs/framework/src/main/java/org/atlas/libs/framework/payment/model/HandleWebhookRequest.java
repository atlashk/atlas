package org.atlas.libs.framework.payment.model;

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
public class HandleWebhookRequest {

  private String rawPayload;
  private Map<String, String> headers;
}
