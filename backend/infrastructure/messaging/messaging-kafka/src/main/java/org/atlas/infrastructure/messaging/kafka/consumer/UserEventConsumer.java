package org.atlas.infrastructure.messaging.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.atlas.infrastructure.messaging.kafka.common.KafkaConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer extends KafkaMessageConsumer {

  public UserEventConsumer(ApplicationContextService applicationContextService) {
    super(applicationContextService);
  }

  @Override
  @KafkaListener(
      topics = KafkaConstant.USER_EVENT_TOPIC,
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, // order-events-retry-0, order-events-retry-1, order-events-retry-2, etc.
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true) // Exponential backoff
  )
  public void consumeMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }
}
