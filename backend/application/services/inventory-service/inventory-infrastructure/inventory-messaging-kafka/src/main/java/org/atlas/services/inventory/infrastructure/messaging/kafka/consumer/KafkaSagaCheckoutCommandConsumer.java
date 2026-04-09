package org.atlas.services.inventory.infrastructure.messaging.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.atlas.libs.framework.saga.core.command.SagaCommandDispatcher;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.messaging.kafka.consumer.BaseKafkaMessageConsumer;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSagaCheckoutCommandConsumer extends BaseKafkaMessageConsumer {

  private final SagaCommandDispatcher dispatcher;

  @KafkaListener(
      topics = "saga.checkout.command.inventory",
      containerFactory = "defaultContainerFactory"
  )
  // Non-blocking retry
  @RetryableTopic(
      attempts = "4", // max retries is 3
      exclude = {ClassCastException.class},
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      backOff = @BackOff(delay = 1000, multiplier = 2, jitter = 200)
  )
  public void consumeCheckoutCommand(ConsumerRecord<String, Object> record, Acknowledgment ack) {
    super.consumeMessage(record, ack);
  }

  @Override
  protected void handleMessage(Object payload) {
    SagaCommand sagaCommand =
        JsonUtil.toObject((String) payload, SagaCommand.class);
    dispatcher.dispatch(sagaCommand);
  }
}
