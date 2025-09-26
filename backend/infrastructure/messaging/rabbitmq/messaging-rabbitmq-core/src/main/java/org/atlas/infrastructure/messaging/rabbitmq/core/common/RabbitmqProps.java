package org.atlas.infrastructure.messaging.rabbitmq.core.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.messaging.rabbitmq")
@Getter
@Setter
public class RabbitmqProps {

  private List<ExchangeConfig> exchanges;
  private List<QueueConfig> queues;
  private List<BindingConfig> bindings;

  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public enum ExchangeType {
    DIRECT("direct"),
    FANOUT("fanout"),
    TOPIC("topic"),
    HEADERS("headers");

    private final String value;
  }

  @Getter
  @Setter
  public static class ExchangeConfig {

    private String name;
    private ExchangeType type;
    private boolean durable = true;
    private boolean autoDelete = true;
    private Map<String, Object> arguments = new HashMap<>();
  }

  @Getter
  @Setter
  public static class QueueConfig {

    private String name;
    private boolean durable = true;
    private boolean exclusive = true;
    private boolean autoDelete = true;
    private Map<String, Object> arguments = new HashMap<>();
  }

  @Getter
  @Setter
  public static class BindingConfig {

    private String exchange;
    private String queue;
    private String routingKey;
    private Map<String, Object> arguments = new HashMap<>();
  }
}
