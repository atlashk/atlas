package org.atlas.services.catalog.infrastructure.messaging.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;
import org.atlas.libs.framework.domain.event.handler.DomainEventDispatcher;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.messaging.rabbitmq.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqStockEventConsumer extends BaseRabbitmqMessageConsumer {

  private final DomainEventDispatcher dispatcher;

  @RabbitListener(
      queues = "stock_events",
      containerFactory = "customContainerFactory"
  )
  public void consumeStockEvents(@Payload Object payload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(payload, deliveryTag, routingKey, exchange, channel);
  }

  @Override
  protected void handleMessage(Object payload) {
    StockStatusChangedEvent event =
        JsonUtil.toObject((String) payload, StockStatusChangedEvent.class);
    dispatcher.dispatch(event);
  }
}
