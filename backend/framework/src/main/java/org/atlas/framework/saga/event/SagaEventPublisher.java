package org.atlas.framework.saga.event;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

  private final MessagePublisherPort messagePublisherPort;

  public void publish(SagaCommandEvent event) {
    final String destination = String.format("saga.%s.command", event.getSagaName())
        .toLowerCase();
    messagePublisherPort.publish(destination, String.valueOf(event.getSagaId()), event);
  }

  public void publish(SagaCommandReplyEvent event) {
    final String destination = String.format("saga.%s.command.reply", event.getSagaName())
        .toLowerCase();
    messagePublisherPort.publish(destination, String.valueOf(event.getSagaId()), event);
  }

  public void publish(SagaCommandCompensationEvent event) {
    final String destination = String.format("saga.%s.command.compensation", event.getSagaName())
        .toLowerCase();
    messagePublisherPort.publish(destination, String.valueOf(event.getSagaId()), event);
  }

  public void publish(SagaCommandCompensationReplyEvent event) {
    final String destination = String.format("saga.%s.command.compensation.reply",
            event.getSagaName())
        .toLowerCase();
    messagePublisherPort.publish(destination, String.valueOf(event.getSagaId()), event);
  }
}
