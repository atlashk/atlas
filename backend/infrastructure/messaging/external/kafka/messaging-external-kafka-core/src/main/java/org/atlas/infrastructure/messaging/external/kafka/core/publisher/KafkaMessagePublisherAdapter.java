package org.atlas.infrastructure.messaging.external.kafka.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.messaging.external.kafka.core.common.KafkaConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessagePublisherAdapter implements ExternalMessagePublisherPort {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publish(BaseOrderEvent event) {
    this.doPublish(event, String.valueOf(event.getOrderId()), KafkaConstant.TOPIC_ORDER_EVENT);
  }

  @Override
  public void publish(BaseProductEvent event) {
    this.doPublish(event, String.valueOf(event.getProductId()), KafkaConstant.TOPIC_PRODUCT_EVENT);
  }

  @Override
  public void publish(BaseUserEvent event) {
    this.doPublish(event, String.valueOf(event.getUserId()), KafkaConstant.TOPIC_USER_EVENT);
  }

  @Override
  public void doPublish(Object messagePayload, String messageKey, String topic) {
    if (StringUtil.isBlank(topic)) {
      throw new IllegalArgumentException("Topic must be specified");
    }

    // Asynchronous send
    kafkaTemplate.send(topic, messageKey, messagePayload)
        .whenCompleteAsync((result, throwable) ->
            logResult(messagePayload, topic, result, throwable));
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
