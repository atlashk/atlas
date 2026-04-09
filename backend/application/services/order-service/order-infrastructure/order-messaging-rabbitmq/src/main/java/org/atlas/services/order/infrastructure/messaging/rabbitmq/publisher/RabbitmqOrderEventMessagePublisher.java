package org.atlas.services.order.infrastructure.messaging.rabbitmq.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.contract.order.OrderExpiredEvent;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.services.order.port.out.messaging.OrderEventMessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqOrderEventMessagePublisher implements OrderEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(OrderExpiredEvent event) {
    final String exchange = "order_events";
    final String routingKey = "order_events";
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
