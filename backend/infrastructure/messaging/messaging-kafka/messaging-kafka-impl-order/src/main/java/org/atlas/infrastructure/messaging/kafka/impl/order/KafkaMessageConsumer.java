package org.atlas.infrastructure.messaging.kafka.impl.order;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.infrastructure.domain.event.handler.DomainEventDispatcher;
import org.atlas.infrastructure.messaging.kafka.core.common.KafkaConstant;
import org.atlas.infrastructure.messaging.kafka.core.consumer.BaseKafkaMessageConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageConsumer extends BaseKafkaMessageConsumer {

  public KafkaMessageConsumer(DomainEventDispatcher domainEventDispatcher) {
    super(domainEventDispatcher);
  }

  @KafkaListener(
      topics = KafkaConstant.TOPIC_ORDER_EVENT,
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE, // order-events-retry-0, order-events-retry-1, order-events-retry-2, etc.
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true) // Exponential backoff
  )
  public void consumeOrderEvent(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }
}
