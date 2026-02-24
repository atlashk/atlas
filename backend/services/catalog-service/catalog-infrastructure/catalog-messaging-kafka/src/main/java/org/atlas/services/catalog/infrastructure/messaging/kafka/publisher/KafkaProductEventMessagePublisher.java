package org.atlas.services.catalog.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.services.catalog.port.out.messaging.ProductEventMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(ProductCreatedEvent event) {
    Message message = Message.builder()
        .destination(KafkaTopics.PRODUCT_EVENTS)
        .routingAttributes(Map.of("messageKey", event.getProductId()))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
