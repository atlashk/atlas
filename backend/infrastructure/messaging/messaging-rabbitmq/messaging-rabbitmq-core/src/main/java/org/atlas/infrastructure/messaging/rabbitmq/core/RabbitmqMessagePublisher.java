package org.atlas.infrastructure.messaging.rabbitmq.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.gateway.MessagePublisher;
import org.atlas.framework.util.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqMessagePublisher implements MessagePublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(Object messagePayload, String routingKey, String exchange) {
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
