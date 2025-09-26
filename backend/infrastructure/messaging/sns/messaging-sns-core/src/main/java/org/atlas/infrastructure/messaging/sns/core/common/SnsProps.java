package org.atlas.infrastructure.messaging.sns.core.common;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.messaging.sns")
@Getter
@Setter
public class SnsProps {

  private Map<String, String> snsTopicArn;
  private Map<String, String> sqsQueueUrl;
  private Integer batchSize;
  private Integer waitTimeSeconds;
  private Integer visibilityTimeout;
}
