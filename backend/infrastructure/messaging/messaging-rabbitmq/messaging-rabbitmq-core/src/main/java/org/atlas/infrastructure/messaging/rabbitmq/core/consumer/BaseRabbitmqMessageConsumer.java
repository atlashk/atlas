package org.atlas.infrastructure.messaging.rabbitmq.core.consumer;

import com.rabbitmq.client.Channel;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.util.ReflectionUtil;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.aop.support.AopUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@RequiredArgsConstructor
@Slf4j
public class BaseRabbitmqMessageConsumer {

  private final ApplicationContextService applicationContextService;

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
      log.error("Failed to process message: payload={}, error={}", messagePayload, e.getMessage(),
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

  private void handleMessage(Object messagePayload) {
    DomainEvent domainEvent = (DomainEvent) messagePayload;
    DomainEventType domainEventType = domainEvent.getDomainEventType();
    Object domainEventHandler = applicationContextService.getBeanByAnnotationAttribute(
            DomainEventHandler.class, DomainEventType.class, "type", domainEventType)
        .orElseThrow(() -> new RuntimeException(
            "Domain event handler not found for type " + domainEventType));

    // Get the target class to handle CGLIB proxies
    Class<?> targetClass = AopUtils.isAopProxy(domainEventHandler)
        ? AopUtils.getTargetClass(domainEventHandler)
        : domainEventHandler.getClass();

    ReflectionUtil.invokeMethod(domainEventHandler, targetClass, "handle",
        Map.of(domainEvent.getClass(), domainEvent));
  }
}
