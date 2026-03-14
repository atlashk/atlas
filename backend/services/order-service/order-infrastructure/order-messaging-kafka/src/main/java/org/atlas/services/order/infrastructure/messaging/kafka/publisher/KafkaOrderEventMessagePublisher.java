package org.atlas.services.order.infrastructure.messaging.kafka.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.libs.framework.domain.event.contract.order.OrderExpiredEvent;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.messaging.kafka.common.KafkaTopics;
import org.atlas.services.order.port.out.messaging.OrderEventMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaOrderEventMessagePublisher implements OrderEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(OrderExpiredEvent event) {
    Message message = Message.builder()
        .destination(KafkaTopics.ORDER_EVENTS)
        .routingAttributes(Map.of("messageKey", event.getOrderId()))
        .payload(JsonUtil.toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
