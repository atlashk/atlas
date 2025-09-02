package org.atlas.infrastructure.messaging.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.messaging.ProductMessagePublisherPort;
import org.atlas.framework.messaging.gateway.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.common.KafkaConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMessagePublisherAdapter implements ProductMessagePublisherPort {

  private final MessageGateway messageGateway;

  @Override
  public void publish(BaseProductEvent event) {
    messageGateway.send(event, String.valueOf(event.getProductId()),
        KafkaConstant.PRODUCT_EVENT_TOPIC);
  }
}
