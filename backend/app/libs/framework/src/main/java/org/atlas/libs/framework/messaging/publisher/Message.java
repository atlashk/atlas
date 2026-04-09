package org.atlas.libs.framework.messaging.publisher;

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
public class Message {

  private String destination;
  private Map<String, Object> routingAttributes;
  private String payload;
  private Map<String, Object> headers;
}
