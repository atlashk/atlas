package org.atlas.infrastructure.messaging.kafka.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.util.MapUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessagePublisher implements MessagePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public final void publish(PublishRequest request) {
    // Extract topic name
    final String topic = request.getDestination();
    if (StringUtil.isBlank(topic)) {
      throw new IllegalArgumentException("Topic must be specified");
    }

    // Extract message key if any
    final String messageKey = request.getRoutingAttributes().get("messageKey").toString();

    // Convert headers to Kafka headers
    Headers kafkaHeaders = new RecordHeaders();
    if (MapUtil.isNotEmpty(request.getHeaders())) {
      request.getHeaders().forEach((key, value) ->
          kafkaHeaders.add(key, value != null ? value.toString().getBytes() : null));
    }

    // Create ProducerRecord with headers
    ProducerRecord<String, Object> record =
        new ProducerRecord<>(topic, null, messageKey, request.getMessagePayload(), kafkaHeaders);

    // Asynchronous send
    kafkaTemplate.send(record)
        .whenCompleteAsync((result, throwable) ->
            logResult(request.getMessagePayload(), topic, result, throwable));
  }

  private void logResult(Object messagePayload, String topic, SendResult<String, Object> result,
      Throwable throwable) {
    if (throwable == null) {
      log.info("Published message: {}\nTopic: {}. Partition: {}. Offset: {}",
          messagePayload, topic, result.getRecordMetadata().partition(),
          result.getRecordMetadata().offset());
    } else {
      log.error("Failed to publish message: {}\nTopic: {}",
          messagePayload, topic, throwable);
    }
  }
}
