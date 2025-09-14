package org.atlas.infrastructure.messaging.kafka.core.consumer;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.util.ReflectionUtil;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.springframework.aop.support.AopUtils;
import org.springframework.kafka.support.Acknowledgment;

@RequiredArgsConstructor
@Slf4j
public class BaseKafkaMessageConsumer {

  private final ApplicationContextService applicationContextService;

  protected void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    log.info("Consumed record: payload={}, partition={}, offset={}",
        record.value(), record.partition(), record.offset());
    Object messagePayload = record.value();
    handleMessage(messagePayload);

    // Manually commit offset after handling
    ack.acknowledge();
  }

  private void handleMessage(Object messagePayload) {
    // Find the handler based on domain event type
    DomainEvent domainEvent = (DomainEvent) messagePayload;
    DomainEventType domainEventType = domainEvent.getDomainEventType();
    Optional<Object> domainEventHandlerOpt = applicationContextService.getBeanByAnnotationAttribute(
        DomainEventHandler.class, DomainEventType.class, "type", domainEventType);
    if (domainEventHandlerOpt.isEmpty()) {
      // Skip handling
      return;
    }
    Object domainEventHandler = domainEventHandlerOpt.get();

    // Get the target class to handle CGLIB proxies
    Class<?> targetClass = AopUtils.isAopProxy(domainEventHandler)
        ? AopUtils.getTargetClass(domainEventHandler)
        : domainEventHandler.getClass();

    ReflectionUtil.invokeMethod(domainEventHandler, targetClass, "handle",
        Map.of(domainEvent.getClass(), domainEvent));
  }
}
