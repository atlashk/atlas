package org.atlas.infrastructure.messaging.kafka.impl.product.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.infrastructure.messaging.kafka.core.constant.KafkaTopics;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(ProductEvent event) {
    Message message = Message.builder()
        .destination(KafkaTopics.PRODUCT_EVENTS)
        .routingAttributes(Map.of("messageKey", event.getProductId()))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
