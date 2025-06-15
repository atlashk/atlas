package org.atlas.infrastructure.messaging.kafka.core;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.atlas.framework.messaging.MessagePublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessagePublisher implements MessagePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ContextSnapshotFactory contextSnapshotFactory;

  @Override
  public void publish(Object messagePayload, String messageKey, String topic) {
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("Topic must be specified");
    }

    // Capture MDC context before async operation
    ContextSnapshot snapshot = contextSnapshotFactory.captureAll();

    // Asynchronous send
    kafkaTemplate.send(topic, messageKey, messagePayload)
        .whenCompleteAsync((result, throwable) -> {
          // Restore context (including MDC) in the callback
          try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
            if (throwable == null) {
              log.info("Published message: {}\nTopic: {}. Partition: {}. Offset: {}",
                  messagePayload, topic, result.getRecordMetadata().partition(),
                  result.getRecordMetadata().offset());
            } else {
              log.error("Failed to publish message: {}\nTopic: {}. Error: {}",
                  messagePayload, topic, throwable.getMessage(), throwable);
            }
          }
        });
  }
}
