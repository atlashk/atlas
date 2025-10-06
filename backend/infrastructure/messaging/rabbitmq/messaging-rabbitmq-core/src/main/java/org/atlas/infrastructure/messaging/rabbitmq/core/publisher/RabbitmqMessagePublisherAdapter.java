package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqMessagePublisherAdapter implements MessagePublisherPort {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(String exchange, String routingKey, Object messagePayload) {
    if (StringUtil.isBlank(exchange)) {
      throw new IllegalArgumentException("Exchange must be specified");
    }

    try {
      rabbitTemplate.convertAndSend(exchange, routingKey, messagePayload);

      log.info("Published message: {}\nExchange: {}. Routing Key: {}",
          messagePayload, exchange, routingKey);
    } catch (Exception e) {
      log.error("Failed to publish message: {}\nExchange: {}. Routing Key: {}",
          messagePayload, exchange, routingKey, e);
      throw e;
    }
  }
}
