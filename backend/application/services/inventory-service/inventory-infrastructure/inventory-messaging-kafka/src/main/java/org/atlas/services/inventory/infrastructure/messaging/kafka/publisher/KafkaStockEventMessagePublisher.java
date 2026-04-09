package org.atlas.services.inventory.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.services.inventory.port.out.messaging.StockEventMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaStockEventMessagePublisher implements StockEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(StockStatusChangedEvent event) {
    Message message = Message.builder()
        .destination(KafkaTopics.STOCK_EVENTS)
        .routingAttributes(Map.of("messageKey", event.getProductId()))
        .payload(JsonUtil.toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
