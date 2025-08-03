package org.atlas.infrastructure.messaging.kafka.core;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.MessagePublisher;
import org.atlas.framework.util.StringUtil;
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
    if (StringUtil.isBlank(topic)) {
      throw new IllegalArgumentException("Topic must be specified");
    }

    // Use reflection to capture context without compile-time dependency
    ContextSnapshot contextSnapshot = contextSnapshotFactory.captureAll();

    // Asynchronous send with context restoration
    kafkaTemplate.send(topic, messageKey, messagePayload)
        .whenCompleteAsync((result, throwable) -> {
          try (var scope = contextSnapshot.setThreadLocals()) {
            logResult(messagePayload, topic, result, throwable);
          } catch (Exception e) {
            log.warn("Failed to restore context, logging without context", e);
            logResult(messagePayload, topic, result, throwable);
          }
        });
  }

  private void logResult(Object messagePayload, String topic,
      org.springframework.kafka.support.SendResult<String, Object> result,
      Throwable throwable) {
    if (throwable == null) {
      log.info("Published message: {}\nTopic: {}. Partition: {}. Offset: {}",
          messagePayload, topic, result.getRecordMetadata().partition(),
          result.getRecordMetadata().offset());
    } else {
      log.error("Failed to publish message: {}\nTopic: {}. Error: {}",
          messagePayload, topic, throwable.getMessage(), throwable);
    }
  }
}
