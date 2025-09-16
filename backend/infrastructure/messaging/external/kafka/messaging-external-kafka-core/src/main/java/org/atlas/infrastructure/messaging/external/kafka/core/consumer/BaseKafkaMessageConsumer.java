package org.atlas.infrastructure.messaging.external.kafka.core.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.infrastructure.domain.event.handler.DomainEventDispatcher;
import org.springframework.kafka.support.Acknowledgment;

@RequiredArgsConstructor
@Slf4j
public class BaseKafkaMessageConsumer {

  private final DomainEventDispatcher domainEventDispatcher;

  protected void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    log.info("Consumed record: payload={}, partition={}, offset={}",
        record.value(), record.partition(), record.offset());

    // Handle message
    Object messagePayload = record.value();
    domainEventDispatcher.dispatch(messagePayload);

    // Manually commit offset after handling
    ack.acknowledge();
  }
}
