package org.atlas.product.messaging.rabbitmq.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.product.application.port.messaging.ProductEventMessagePublisher;
import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.messaging.publisher.Message;
import org.atlas.common.framework.messaging.publisher.MessagePublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(ProductEvent event) {
    final String exchange = "product_events";
    final String routingKey = "product_events";
    Message message = Message.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .payload(JsonUtil.getInstance().toJson(event))
        .build();
    messagePublisher.publish(message);
  }
}
