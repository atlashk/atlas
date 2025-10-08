package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.util.MapUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqMessagePublisherAdapter implements MessagePublisherPort {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(PublishRequest request) {
    // Extract exchange name
    final String exchange = request.getDestination();
    if (StringUtil.isBlank(exchange)) {
      throw new IllegalArgumentException("Exchange must be specified");
    }

    // Determine routing key based on exchange type
    String routingKey = StringUtil.EMPTY;
    Map<String, Object> routingAttributes = request.getRoutingAttributes();
    if (routingAttributes != null && routingAttributes.containsKey("routingKey")) {
      routingKey = routingAttributes.get("routingKey").toString();
    }

    try {
      if (MapUtil.isNotEmpty(request.getHeaders())) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request.getMessagePayload(), message -> {
          request.getHeaders().forEach((key, value) -> {
            if (value != null) {
              message.getMessageProperties().setHeader(key, value);
            }
          });
          return message;
        });
      } else {
        rabbitTemplate.convertAndSend(exchange, routingKey, request.getMessagePayload());
      }

      log.info("Published message: {}\nExchange: {}. Routing Key: {}",
          request.getMessagePayload(), exchange, routingKey);
    } catch (Exception e) {
      log.error("Failed to publish message: {}\nExchange: {}. Routing Key: {}",
          request.getMessagePayload(), exchange, routingKey, e);
      throw e;
    }
  }
}
