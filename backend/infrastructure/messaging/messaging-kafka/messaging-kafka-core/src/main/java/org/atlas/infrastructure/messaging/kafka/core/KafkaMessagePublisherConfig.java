package org.atlas.infrastructure.messaging.kafka.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.atlas.framework.messaging.InstantMessageGateway;
import org.atlas.framework.messaging.MessageGateway;
import org.atlas.framework.messaging.MessagePublisher;
import org.atlas.framework.messaging.outbox.OutboxMessageGateway;
import org.atlas.framework.messaging.outbox.OutboxMessageRepository;
import org.atlas.framework.messaging.outbox.RelayOutboxMessageTask;
import org.atlas.framework.transaction.TransactionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaMessagePublisherConfig {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();

    // === Basic configuration ===
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    // === Reliability settings ===

    // Wait for all replicas to acknowledge before considering the message as sent
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");

    // Retry sending the message up to 3 times in case of failure
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

    // Wait 1 second between retry attempts
    configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

    // === Idempotence (Exactly-once semantics) ===

    // Ensures no duplicates during retries by tracking message sequence numbers
    // This requires max.in.flight.requests.per.connection (default to 5) to be less than or equal to 5.
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

    // === Performance tuning ===

    // The producer will try to batch records together into fewer requests to improve performance
    configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16 KB

    // Linger time — how long the producer waits before sending a batch (to allow more messages to be added)
    configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // 10 milliseconds

    // Total memory available to the producer for buffering (in bytes)
    configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32 MB

    // Compress messages using Snappy (can improve performance and reduce network usage)
    configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
    // Enable micrometer tracing that will propagate context to consumer
    kafkaTemplate.setObservationEnabled(true);
    return kafkaTemplate;
  }

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
