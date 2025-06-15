package org.atlas.infrastructure.messaging.kafka.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
public abstract class KafkaMessageConsumer {

  protected void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    log.info("Consumed record: payload={}, partition={}, offset={}",
        record.value(), record.partition(), record.offset());
    Object messagePayload = record.value();
    handleMessage(messagePayload);

    // Manually commit offset after handling
    ack.acknowledge();
  }

  protected abstract void handleMessage(Object messagePayload);
}
