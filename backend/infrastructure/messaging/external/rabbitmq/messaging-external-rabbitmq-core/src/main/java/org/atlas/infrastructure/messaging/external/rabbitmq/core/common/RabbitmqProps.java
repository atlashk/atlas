package org.atlas.infrastructure.messaging.external.rabbitmq.core.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.messaging.rabbitmq")
@Data
public class RabbitmqProps {

  private List<ExchangeConfig> exchanges;
  private List<QueueConfig> queues;
  private List<BindingConfig> bindings;

  @Data
  public static class ExchangeConfig {

    private final String name;
    private final ExchangeType type;
    private final boolean durable = true;
    private final boolean autoDelete = true;
    private Map<String, Object> arguments = new HashMap<>();
  }

  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum ExchangeType {
    DIRECT("direct"),
    FANOUT("fanout"),
    TOPIC("topic"),
    HEADERS("headers");

    private final String value;
  }

  @Data
  public static class QueueConfig {

    private final String name;
    private final boolean durable = true;
    private final boolean exclusive = true;
    private final boolean autoDelete = true;
    private Map<String, Object> arguments = new HashMap<>();
  }

  @Data
  public static class BindingConfig {

    private final String exchange;
    private final String queue;
    private final String routingKey;
    private Map<String, Object> arguments = new HashMap<>();
  }
}
