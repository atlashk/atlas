package org.atlas.infrastructure.messaging.rabbitmq.impl.saga.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.event.SagaEvent;
import org.atlas.framework.saga.event.SagaEventResolver;
import org.atlas.infrastructure.messaging.rabbitmq.core.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaEventConsumer extends BaseRabbitmqMessageConsumer {

  private final SagaEventResolver sagaEventResolver;

  @Override
  protected void handleMessage(Object messagePayload) {
    sagaEventResolver.resolve((SagaEvent) messagePayload);
  }

  @RabbitListener(
      queues = RabbitmqConstant.QUEUE_ORDER_SVC_SAGA_CHECKOUT_COMMAND,
      containerFactory = "customContainerFactory"
  )
  public void consumeCheckoutCommand(@Payload Object messagePayload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(messagePayload, deliveryTag, routingKey, exchange, channel);
  }
}
