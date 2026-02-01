package org.atlas.services.order.infrastructure.messaging.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensationReply;
import org.atlas.libs.framework.saga.core.orchestrator.SagaOrchestrator;
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
public class KafkaSagaCheckoutCompensationReplyConsumer extends BaseKafkaMessageConsumer {

  private final SagaOrchestrator sagaOrchestrator;

  @KafkaListener(
      topics = "saga.checkout.compensationreply",
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      exclude = {ClassCastException.class},
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
  )
  public void consumeCheckoutCompensationReply(ConsumerRecord<String, Object> record,
      Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }

  @Override
  protected void handleMessage(Object payload) {
    SagaCompensationReply sagaCompensationReply =
        JsonUtil.getInstance().toObject((String) payload, SagaCompensationReply.class);
    sagaOrchestrator.handleSagaCompensationReply(sagaCompensationReply);
  }
}
