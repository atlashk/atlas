package org.atlas.services.inventory.infrastructure.messaging.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.libs.framework.domain.event.handler.DomainEventDispatcher;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.libs.messaging.kafka.consumer.BaseKafkaMessageConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventConsumer extends BaseKafkaMessageConsumer {

  private final DomainEventDispatcher dispatcher;

  @KafkaListener(
      topics = KafkaTopics.ORDER_EVENTS,
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      exclude = {ClassCastException.class},
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
  )
  public void consumeOrderEvents(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }

  @Override
  protected void handleMessage(Object payload) {
    dispatcher.dispatch(payload);
  }
}
