package org.atlas.common.infrastructure.messaging.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseRabbitmqMessageConsumer {

  protected abstract void handleMessage(Object payload);

  protected void consumeMessage(@Payload Object payload,
      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
      @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
      @Header(AmqpHeaders.RECEIVED_EXCHANGE) String exchange,
      Channel channel) {
    log.info("Consumed message: payload={}, exchange={}, routingKey={}, deliveryTag={}",
        payload, exchange, routingKey, deliveryTag);

    try {
      // Handle message
      handleMessage(payload);

      // Manually acknowledge the message after successful processing
      channel.basicAck(deliveryTag, false);
      log.debug("Message acknowledged: deliveryTag={}", deliveryTag);
    } catch (Exception e) {
      log.error("Failed to process message: payload={}, error={}", payload, e.getMessage(),
          e);
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
}
