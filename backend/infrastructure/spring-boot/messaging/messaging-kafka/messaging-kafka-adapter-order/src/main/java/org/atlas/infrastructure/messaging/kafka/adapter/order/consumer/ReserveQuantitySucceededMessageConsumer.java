package org.atlas.infrastructure.messaging.kafka.adapter.order.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.domain.order.event.ReserveQuantitySucceededEventHandler;
import org.atlas.framework.domain.event.contract.product.ReserveQuantitySucceededEvent;
import org.atlas.infrastructure.messaging.kafka.core.KafkaMessageConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveQuantitySucceededMessageConsumer extends KafkaMessageConsumer {

  private final ReserveQuantitySucceededEventHandler reserveQuantitySucceededEventHandler;

  @Override
  @KafkaListener(
      topics = "#{kafkaProps.topic.reserveQuantitySucceededEvent}",
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

  @Override
  protected void handleMessage(Object messagePayload) {
    reserveQuantitySucceededEventHandler.handle((ReserveQuantitySucceededEvent) messagePayload);
  }
}
