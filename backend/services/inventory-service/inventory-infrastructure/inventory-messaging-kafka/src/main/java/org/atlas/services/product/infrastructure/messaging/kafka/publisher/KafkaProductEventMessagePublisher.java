package org.atlas.services.product.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.services.product.port.out.messaging.ProductEventMessagePublisher;
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
