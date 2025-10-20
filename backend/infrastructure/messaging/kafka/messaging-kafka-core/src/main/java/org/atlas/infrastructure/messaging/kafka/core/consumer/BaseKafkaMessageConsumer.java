package org.atlas.infrastructure.messaging.kafka.core.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseKafkaMessageConsumer {

  protected abstract void handleMessage(Object payload);

  protected void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    log.info("Consumed record: payload={}, partition={}, offset={}",
        record.value(), record.partition(), record.offset());

    // Handle message
    Object payload = record.value();
    handleMessage(payload);

    // Manually commit offset after handling
    ack.acknowledge();
  }
}
