package org.atlas.infrastructure.messaging.rabbitmq.adapter.order.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.core.orchestrator.SagaOrchestrator;
import org.atlas.infrastructure.messaging.rabbitmq.core.consumer.BaseRabbitmqMessageConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaCheckoutCommandReplyConsumer extends BaseRabbitmqMessageConsumer {

  private final SagaOrchestrator sagaOrchestrator;

  @RabbitListener(
      queues = "saga.checkout.commandreply",
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
    SagaCommandReply sagaCommandReply =
        JsonUtil.getInstance().toObject((String) payload, SagaCommandReply.class);
    sagaOrchestrator.handleSagaCommandReply(sagaCommandReply);
  }
}
