package org.atlas.infrastructure.messaging.rabbitmq.impl.product.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.core.compensation.SagaCompensationDispatcher;
import org.atlas.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.infrastructure.messaging.rabbitmq.core.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaCheckoutCompensationConsumer extends BaseRabbitmqMessageConsumer {

  private final SagaCompensationDispatcher dispatcher;

  @RabbitListener(
      queues = "saga.checkout.compensation.product",
      containerFactory = "customContainerFactory"
  )
  public void consumeCheckoutCompensation(@Payload Object payload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    super.consumeMessage(payload, deliveryTag, routingKey, exchange, channel);
  }

  @Override
  protected void handleMessage(Object payload) {
    SagaCompensation sagaCompensation =
        JsonUtil.getInstance().toObject((String) payload, SagaCompensation.class);
    dispatcher.dispatch(sagaCompensation);
  }
}
