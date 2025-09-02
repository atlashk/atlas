package org.atlas.infrastructure.messaging.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.messaging.OrderMessagePublisherPort;
import org.atlas.framework.messaging.gateway.MessageGateway;
import org.atlas.infrastructure.messaging.kafka.common.KafkaConstant;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMessagePublisherAdapter implements OrderMessagePublisherPort {

  private final MessageGateway messageGateway;

  @Override
  public void publish(BaseOrderEvent event) {
    messageGateway.send(event, String.valueOf(event.getOrderId()), KafkaConstant.ORDER_EVENT_TOPIC);
  }
}
