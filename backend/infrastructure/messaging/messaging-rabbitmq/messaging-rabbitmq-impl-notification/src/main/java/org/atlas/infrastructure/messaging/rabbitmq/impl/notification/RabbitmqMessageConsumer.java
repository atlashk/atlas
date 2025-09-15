package org.atlas.infrastructure.messaging.rabbitmq.impl.notification;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.atlas.infrastructure.domain.event.handler.DomainEventDispatcher;
import org.atlas.infrastructure.messaging.rabbitmq.core.common.RabbitmqConstant;
import org.atlas.infrastructure.messaging.rabbitmq.core.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitmqMessageConsumer extends BaseRabbitmqMessageConsumer {

  public RabbitmqMessageConsumer(DomainEventDispatcher domainEventDispatcher) {
    super(domainEventDispatcher);
  }

  @RabbitListener(
      queues = RabbitmqConstant.QUEUE_NOTIFICATION_SVC_ORDER_EVENT,
      containerFactory = "customContainerFactory"
  )
  public void consumeOrderEvent(@Payload Object messagePayload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(messagePayload, deliveryTag, routingKey, exchange, channel);
  }
}
