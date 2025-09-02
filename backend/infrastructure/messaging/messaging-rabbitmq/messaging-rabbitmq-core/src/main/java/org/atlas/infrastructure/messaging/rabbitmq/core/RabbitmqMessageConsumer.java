package org.atlas.infrastructure.messaging.rabbitmq.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import com.rabbitmq.client.Channel;

@Slf4j
public abstract class RabbitmqMessageConsumer {

  protected void consumeMessage(@Payload Object messagePayload, 
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    log.info("Consumed message: payload={}, exchange={}, routingKey={}, deliveryTag={}",
        messagePayload, exchange, routingKey, deliveryTag);

    try {
      handleMessage(messagePayload);

      // Manually acknowledge the message after successful processing
      channel.basicAck(deliveryTag, false);
      log.debug("Message acknowledged: deliveryTag={}", deliveryTag);
    } catch (Exception e) {
      log.error("Failed to process message: payload={}, error={}", messagePayload, e.getMessage(), e);
      try {
        // Reject the message and requeue it for retry
        channel.basicNack(deliveryTag, false, true);
        log.warn("Message rejected and requeued: deliveryTag={}", deliveryTag);
      } catch (Exception ackException) {
        log.error("Failed to reject message: deliveryTag={}, error={}", 
            deliveryTag, ackException.getMessage(), ackException);
      }
    }
  }

  protected abstract void handleMessage(Object messagePayload);
}
