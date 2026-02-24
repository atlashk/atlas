package org.atlas.services.inventory.infrastructure.messaging.rabbitmq.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.services.inventory.port.out.messaging.StockEventMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqStockEventMessagePublisher implements StockEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(StockStatusChangedEvent event) {
    final String exchange = "inventory_events";
    final String routingKey = "inventory_events";
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
