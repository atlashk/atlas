package org.atlas.infrastructure.messaging.kafka.consumer;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.util.ReflectionUtil;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.springframework.kafka.support.Acknowledgment;

@RequiredArgsConstructor
@Slf4j
public abstract class KafkaMessageConsumer {

  private final ApplicationContextService applicationContextService;

  protected void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    log.info("Consumed record: payload={}, partition={}, offset={}",
        record.value(), record.partition(), record.offset());
    Object messagePayload = record.value();
    handleMessage(messagePayload);

    // Manually commit offset after handling
    ack.acknowledge();
  }

  protected void handleMessage(Object messagePayload) {
    DomainEvent domainEvent = (DomainEvent) messagePayload;
    DomainEventType domainEventType = domainEvent.getDomainEventType();
    Object domainEventHandler = applicationContextService.getBeanByAnnotationAttribute(
            DomainEventHandler.class, DomainEventType.class, "type", domainEventType)
        .orElseThrow(() -> new RuntimeException(
            "Domain event handler not found for type " + domainEventType));
    ReflectionUtil.invokeMethod(domainEventHandler, "handle",
        Map.of(DomainEvent.class, domainEvent));
  }
}
