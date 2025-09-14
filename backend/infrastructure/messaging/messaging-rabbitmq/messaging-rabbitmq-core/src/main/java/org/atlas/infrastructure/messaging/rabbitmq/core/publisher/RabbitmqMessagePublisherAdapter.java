package org.atlas.infrastructure.messaging.rabbitmq.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.messaging.MessagePublisherPort;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.messaging.rabbitmq.core.common.RabbitmqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqMessagePublisherAdapter implements MessagePublisherPort {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(BaseOrderEvent event) {
    this.doPublish(event, event.getDomainEventType().name(), RabbitmqConstant.EXCHANGE_ORDER_EVENT);
  }

  @Override
  public void publish(BaseProductEvent event) {
    this.doPublish(event, event.getDomainEventType().name(),
        RabbitmqConstant.EXCHANGE_PRODUCT_EVENT);
  }

  @Override
  public void publish(BaseUserEvent event) {
    this.doPublish(event, event.getDomainEventType().name(), RabbitmqConstant.EXCHANGE_USER_EVENT);
  }

  @Override
  public void doPublish(Object messagePayload, String routingKey, String exchange) {
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
