package org.atlas.infrastructure.messaging.kafka.core;

import java.util.Optional;
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
  private final Optional<Object> contextSnapshotFactory;

  @Override
  public void publish(Object messagePayload, String messageKey, String topic) {
    if (StringUtils.isBlank(topic)) {
      throw new IllegalArgumentException("Topic must be specified");
    }

    if (contextSnapshotFactory.isPresent()) {
      publishWithContext(messagePayload, messageKey, topic);
    } else {
      publishWithoutContext(messagePayload, messageKey, topic);
    }
  }

  private void publishWithContext(Object messagePayload, String messageKey, String topic) {
    try {
      // Use reflection to capture context without compile-time dependency
      Class<?> factoryClass = Class.forName("io.micrometer.context.ContextSnapshotFactory");
      Object factory = contextSnapshotFactory.get();
      Object snapshot = factoryClass.getMethod("captureAll").invoke(factory);

      // Asynchronous send with context restoration
      kafkaTemplate.send(topic, messageKey, messagePayload)
          .whenCompleteAsync((result, throwable) -> {
            try {
              Class<?> snapshotClass = Class.forName("io.micrometer.context.ContextSnapshot");
              Object scope = snapshotClass.getMethod("setThreadLocals").invoke(snapshot);

              try {
                logResult(messagePayload, topic, result, throwable);
              } finally {
                if (scope instanceof AutoCloseable) {
                  ((AutoCloseable) scope).close();
                }
              }
            } catch (Exception e) {
              log.warn("Failed to restore context, logging without context", e);
              logResult(messagePayload, topic, result, throwable);
            }
          });
    } catch (Exception e) {
      log.warn("Failed to capture context, falling back to simple publishing", e);
      publishWithoutContext(messagePayload, messageKey, topic);
    }
  }

  private void publishWithoutContext(Object messagePayload, String messageKey, String topic) {
    // Asynchronous send without context handling
    kafkaTemplate.send(topic, messageKey, messagePayload)
        .whenCompleteAsync((result, throwable) -> logResult(messagePayload, topic, result, throwable));
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
