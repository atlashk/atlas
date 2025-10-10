package org.atlas.infrastructure.messaging.kafka.impl.order.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.atlas.framework.saga.orchestrator.SagaOrchestrator;
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
public class KafkaSagaCheckoutCompensationReplyConsumer extends BaseKafkaMessageConsumer {

  private final SagaOrchestrator sagaOrchestrator;

  @KafkaListener(
      topics = "saga.checkout.compensationreply",
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
  )
  public void consumeCheckoutCompensationReply(ConsumerRecord<String, Object> record,
      Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    sagaOrchestrator.handleSagaCompensationReply((SagaCompensationReply) messagePayload);
  }
}
