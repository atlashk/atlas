package org.atlas.infrastructure.messaging.kafka.impl.product.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.MessageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(BaseProductEvent event) {
    MessageRequest request = MessageRequest.builder()
        .destination("product_events")
        .routingAttributes(Map.of("messageKey", event.getProduct().getId()))
        .messagePayload(event)
        .build();
    messagePublisher.publish(request);
  }
}
