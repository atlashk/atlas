package org.atlas.infrastructure.messaging.kafka.impl.saga.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.saga.event.SagaEvent;
import org.atlas.framework.saga.event.SagaEventPublisherPort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSagaEventPublisherAdapter implements SagaEventPublisherPort {

  private final MessagePublisherPort messagePublisherPort;

  @Override
  public void publish(SagaEvent sagaEvent) {
    final String topic = String.format("saga.%s", sagaEvent.getSagaName()).toLowerCase();
    messagePublisherPort.publish(topic, String.valueOf(sagaEvent.getSagaId()), sagaEvent);
  }
}
