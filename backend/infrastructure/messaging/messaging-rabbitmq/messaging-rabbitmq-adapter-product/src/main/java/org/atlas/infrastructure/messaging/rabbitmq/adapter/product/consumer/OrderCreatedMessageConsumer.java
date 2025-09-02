package org.atlas.infrastructure.messaging.rabbitmq.adapter.product.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.event.OrderCreatedEventHandler;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.infrastructure.messaging.rabbitmq.core.RabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedMessageConsumer extends RabbitmqMessageConsumer {

  private final OrderCreatedEventHandler orderCreatedEventHandler;

  @Override
  @RabbitListener(
      queues = "#{rabbitmqProps.queues.productSvcOrderEvents}",
      containerFactory = "customContainerFactory"
  )
  public void consumeMessage(@Payload Object messagePayload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(messagePayload, deliveryTag, routingKey, exchange, channel);
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    orderCreatedEventHandler.handle((OrderCreatedEvent) messagePayload);
  }
}
