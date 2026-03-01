package org.atlas.libs.messaging.rabbitmq.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.framework.util.MapUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqMessagePublisher implements MessagePublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(Message message) {
    // Extract exchange name
    final String exchange = message.getDestination();
    if (StringUtil.isBlank(exchange)) {
      throw new IllegalArgumentException("Exchange must be specified");
    }

    // Determine routing key based on exchange type
    String routingKey = StringUtil.EMPTY;
    Map<String, Object> routingAttributes = message.getRoutingAttributes();
    if (routingAttributes != null && routingAttributes.containsKey("routingKey")) {
      routingKey = routingAttributes.get("routingKey").toString();
    }

    try {
      if (MapUtil.isNotEmpty(message.getHeaders())) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message.getPayload(),
            rabbitMessage -> {
              message.getHeaders().forEach((key, value) -> {
                if (value != null) {
                  rabbitMessage.getMessageProperties().setHeader(key, value);
                }
              });
              return rabbitMessage;
            });
      } else {
        rabbitTemplate.convertAndSend(exchange, routingKey, message.getPayload());
      }

      log.info("Published message: {}\nExchange: {}. Routing Key: {}",
          message.getPayload(), exchange, routingKey);
    } catch (Exception e) {
      log.error("Failed to publish message: {}\nExchange: {}. Routing Key: {}",
          message.getPayload(), exchange, routingKey, e);
      throw e;
    }
  }
}
