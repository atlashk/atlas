package org.atlas.infrastructure.messaging.kafka.adapter.product.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.port.messaging.ProductMessagePublisherPort;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.ProductDeletedEvent;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.event.contract.product.ReserveQuantityFailedEvent;
import org.atlas.framework.domain.event.contract.product.ReserveQuantitySucceededEvent;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.core.KafkaProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMessagePublisherAdapter implements ProductMessagePublisherPort {

  private final MessageGateway messageGateway;
  private final KafkaProps kafkaProps;

  @Override
  public void publish(ProductCreatedEvent event) {
    messageGateway.send(event, String.valueOf(event.getProductId()),
        kafkaProps.getTopic().getProductCreatedEvent());
  }

  @Override
  public void publish(ProductUpdatedEvent event) {
    messageGateway.send(event, String.valueOf(event.getProductId()),
        kafkaProps.getTopic().getProductUpdatedEvent());
  }

  @Override
  public void publish(ProductDeletedEvent event) {
    messageGateway.send(event, String.valueOf(event.getProductId()),
        kafkaProps.getTopic().getProductDeletedEvent());
  }

  @Override
  public void publish(ReserveQuantitySucceededEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()),
        kafkaProps.getTopic().getReserveQuantitySucceededEvent());
  }

  @Override
  public void publish(ReserveQuantityFailedEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()),
        kafkaProps.getTopic().getReserveQuantityFailedEvent());
  }
}
