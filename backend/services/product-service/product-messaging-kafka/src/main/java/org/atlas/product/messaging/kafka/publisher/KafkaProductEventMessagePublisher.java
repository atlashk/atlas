package org.atlas.product.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.port.messaging.ProductEventMessagePublisher;
import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.messaging.publisher.Message;
import org.atlas.common.framework.messaging.publisher.MessagePublisher;
import org.atlas.common.infrastructure.messaging.kafka.common.KafkaTopics;
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
