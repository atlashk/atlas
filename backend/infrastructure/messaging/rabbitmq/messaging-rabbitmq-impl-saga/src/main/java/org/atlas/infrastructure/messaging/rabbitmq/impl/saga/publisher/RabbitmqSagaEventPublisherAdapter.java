package org.atlas.infrastructure.messaging.rabbitmq.impl.saga.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.saga.event.SagaEvent;
import org.atlas.framework.saga.event.SagaEventPublisherPort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqSagaEventPublisherAdapter implements SagaEventPublisherPort {

  private final MessagePublisherPort messagePublisherPort;

  @Override
  public void publish(SagaEvent sagaEvent) {
    final String exchange = String.format("saga.%s", sagaEvent.getSagaName()).toLowerCase();
    final String routingKey = String.format("saga.%s", sagaEvent.getSagaName()).toLowerCase();
    messagePublisherPort.publish(exchange, routingKey, sagaEvent);
  }
}
