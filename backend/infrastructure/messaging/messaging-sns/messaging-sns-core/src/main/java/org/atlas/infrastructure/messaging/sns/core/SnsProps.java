package org.atlas.infrastructure.messaging.sns.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.messaging.sns")
@Data
public class SnsProps {

  private Endpoints snsTopicArn;
  private Endpoints sqsQueueUrl;
  private Integer batchSize;
  private Integer waitTimeSeconds;
  private Integer visibilityTimeout;

  @Data
  public static class Endpoints {

    private String userRegisteredEvent;
    private String productCreatedEvent;
    private String productUpdatedEvent;
    private String productDeletedEvent;
    private String orderCreatedEvent;
    private String reserveQuantitySucceededEvent;
    private String reserveQuantityFailedEvent;
    private String orderConfirmedEvent;
    private String orderCanceledEvent;
  }
}
