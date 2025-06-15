package org.atlas.infrastructure.messaging.sns.core;

import org.atlas.framework.messaging.InstantMessageGateway;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.framework.messaging.MessagePublisher;
import org.atlas.framework.messaging.outbox.OutboxMessageGateway;
import org.atlas.framework.messaging.outbox.OutboxMessageRepository;
import org.atlas.framework.messaging.outbox.RelayOutboxMessageTask;
import org.atlas.framework.transaction.TransactionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SnsMessagePublisherConfig {

  @Bean
  public MessageGateway instantMessageGateway(MessagePublisher messagePublisher) {
    return new InstantMessageGateway(messagePublisher);
  }

  @Bean
  @Primary
  public MessageGateway outboxMessageGateway(OutboxMessageRepository outboxMessageRepository) {
    return new OutboxMessageGateway(outboxMessageRepository);
  }

  @Bean
  public RelayOutboxMessageTask relayOutboxMessageTask(
      OutboxMessageRepository outboxMessageRepository,
      MessagePublisher messagePublisher,
      TransactionPort transactionPort) {
    return new RelayOutboxMessageTask(outboxMessageRepository, messagePublisher, transactionPort);
  }
}
