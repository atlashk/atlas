package org.atlas.services.inventory.infrastructure.messaging.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.saga.core.command.SagaCommandDispatcher;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.messaging.rabbitmq.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaCheckoutCommandConsumer extends BaseRabbitmqMessageConsumer {

  private final SagaCommandDispatcher dispatcher;

  @RabbitListener(
      queues = "saga.checkout.command.product",
      containerFactory = "customContainerFactory"
  )
  public void consumeCheckoutCommand(@Payload Object payload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(payload, deliveryTag, routingKey, exchange, channel);
  }

  @Override
  protected void handleMessage(Object payload) {
    SagaCommand sagaCommand =
        JsonUtil.getInstance().toObject((String) payload, SagaCommand.class);
    dispatcher.dispatch(sagaCommand);
  }
}
