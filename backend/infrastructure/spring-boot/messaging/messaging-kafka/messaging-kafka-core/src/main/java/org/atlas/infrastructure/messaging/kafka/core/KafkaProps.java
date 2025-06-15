package org.atlas.infrastructure.messaging.kafka.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.messaging.kafka")
@Data
public class KafkaProps {

  private Topic topic;

  @Data
  public static class Topic {

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
