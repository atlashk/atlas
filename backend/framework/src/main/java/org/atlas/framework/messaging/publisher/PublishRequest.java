package org.atlas.framework.messaging.publisher;

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
public class PublishRequest {

  private String destination;
  private Map<String, Object> routingAttributes;
  private Object messagePayload;
  private Map<String, Object> headers;
}
