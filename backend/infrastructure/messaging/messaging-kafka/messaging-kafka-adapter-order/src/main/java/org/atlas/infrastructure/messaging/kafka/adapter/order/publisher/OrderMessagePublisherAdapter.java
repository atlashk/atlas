package org.atlas.infrastructure.messaging.kafka.adapter.order.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.port.messaging.OrderMessagePublisherPort;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.order.OrderConfirmedEvent;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.core.KafkaProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessagePublisherAdapter implements OrderMessagePublisherPort {

  private final MessageGateway messageGateway;
  private final KafkaProps kafkaProps;

  @Override
  public void publish(OrderCreatedEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()),
        kafkaProps.getTopic().getOrderCreatedEvent());
  }

  @Override
  public void publish(OrderConfirmedEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()),
        kafkaProps.getTopic().getOrderConfirmedEvent());
  }

  @Override
  public void publish(OrderCanceledEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()),
        kafkaProps.getTopic().getOrderCanceledEvent());
  }
}
