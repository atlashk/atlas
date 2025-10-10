package org.atlas.infrastructure.messaging.rabbitmq.impl.product.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(BaseProductEvent event) {
    final String exchange = "product_events";
    final String routingKey = "product_events";
    PublishRequest request = PublishRequest.builder()
        .destination(exchange)
        .routingAttributes(Map.of("routingKey", routingKey))
        .messagePayload(event)
        .build();
    messagePublisher.publish(request);
  }
}
