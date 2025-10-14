package org.atlas.infrastructure.messaging.kafka.impl.product.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.framework.saga.compensation.SagaCompensationHandlerDispatcher;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.infrastructure.messaging.kafka.core.consumer.BaseKafkaMessageConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSagaCheckoutCompensationConsumer extends BaseKafkaMessageConsumer {

  private final SagaCompensationHandlerDispatcher dispatcher;

  @KafkaListener(
      topics = "saga.checkout.compensation.product",
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      exclude = {ClassCastException.class},
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
  )
  public void consumeCheckoutCompensation(ConsumerRecord<String, Object> record,
      Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    dispatcher.dispatch((SagaCompensation) messagePayload);
  }
}
