# Messaging

## Atlas notes

- Messaging backend is configurable (Kafka / RabbitMQ) via app-stack (`backend/config/app-stack.*.yml`).
- Saga messaging is built on a generic `MessagePublisher` abstraction with Kafka/RabbitMQ adapters:
  - Kafka publisher: `backend/libs/messaging/messaging-kafka`
  - RabbitMQ publisher: `backend/libs/messaging/messaging-rabbitmq`
- Saga topic/exchange naming follows `saga.<sagaName>.*` (see `KafkaSagaMessagePublisher` / `RabbitmqSagaMessagePublisher`).

## Apache Kafka

### Offset management

Scenario:
1. Consumer A processes messages in Partition 0 up to offset 4, then crashes.
2. Consumer B (same consumer group) takes over and resumes from offset 4 (re-processing or retrying that offset).

**When `enable.auto.commit = true` (default):**

Kafka will automatically commit offsets periodically (default every 5 seconds, configurable via `auto.commit.interval.ms`).

⚙️ Behavior:
- Offset is committed regardless of whether the message was successfully processed or not.
- If a consumer crashes after offset 4 is auto-committed but before processing it, that message is lost (won’t be reprocessed).
- Risk of message loss if processing fails between fetch and commit.

📌 Summary:
- Pro: Simpler setup
- Con: Not safe if you need guaranteed processing (e.g., payments)

**When `enable.auto.commit = false`:**

- You must manually commit the offset after successful processing either via:
  - `ack.acknowledge()` in Spring Kafka
  - or Kafka native API: `consumer.commitSync()` / `commitAsync()`

⚙️ Behavior:
- If consumer crashes before offset is manually committed, message is redelivered when a new consumer takes over.
- Prevents message loss, ensures at-least-once delivery.

📌 Summary:
- Pro: Safe and reliable
- Con: Slightly more complex to implement

By default, Spring Kafka sets `enable.auto.commit = false` and lets you choose the ack mode:

Ack Mode | Commit Timing
RECORD | After each record
BATCH | After each batch of records
TIME | Periodically
MANUAL | Only when you call ack.acknowledge()
MANUAL_IMMEDIATE | Same as MANUAL but commits immediately

### Idempotency

#### Producer idempotency

Kafka Idempotent Producer guarantees that duplicate messages are automatically ignored even if the producer sends the same message multiple times due to:
- Network Failure
- Retries
- Application Restart

How It Works (In Simple Points)
1. Every Producer Session gets a unique PID (Producer ID).
2. Each message gets a Sequence Number (like 0, 1, 2…).
3. Kafka stores the PID + Sequence Number in an internal state table.
4. If the same PID + Sequence combination comes again → Kafka automatically ignores the duplicate message.
5. When the producer restarts → a new PID is assigned, and Kafka automatically ignores old messages..

How to Enable It in Code?

```ini
spring.kafka.producer.enable-idempotence=true
```

#### Consumer Idempotency

1. Kafka’s At-Least-Once Delivery Semantics:
By default, Kafka ensures messages are delivered at least once to consumers (if enable.auto.commit is enabled or manual commits are used with retries). This means a consumer might receive the same message multiple times due to network issues, consumer crashes, or offset commit failures.
For example, if a consumer processes a message but crashes before committing its offset, Kafka will redeliver the same message when the consumer restarts, potentially leading to duplicate processing.
Why Idempotency?: Idempotency ensures that even if a DomainEvent (with a unique eventId) is processed multiple times, the outcome remains the same, preventing side effects like duplicate database writes or incorrect state changes.

2. Consumer Retries for Transient Failures:
Many Kafka consumer applications implement retry logic for transient errors (e.g., temporary database unavailability or network timeouts). If a consumer retries a message, it might process the same DomainEvent multiple times.
Without idempotency, retries could lead to unintended side effects, such as creating multiple records for the same event or updating a resource incorrectly.
Why Idempotency?: Checking the eventId in the preHandle method (using Redis, as discussed) ensures that retries don’t result in duplicate processing.

3. Producer-Side Duplicates:
Kafka producers can send duplicate messages if they retry due to network issues or timeouts, even with idempotent producers enabled (though idempotent producers reduce this risk).
If a producer sends the same DomainEvent multiple times (with the same eventId), the consumer might receive duplicates in the topic.
Why Idempotency?: The consumer can use the eventId to detect and ignore duplicates, ensuring that only the first processing of the event has an effect.

4. Consumer Group Rebalancing:
In Kafka, consumer group rebalancing occurs when consumers join/leave the group or when partitions are reassigned. During rebalancing, messages may be redelivered to a different consumer instance, leading to potential duplicate processing.
For example, if a consumer processes a message but doesn’t commit the offset before a rebalance, another consumer may process the same message.
Why Idempotency?: Idempotency checking in the preHandle method ensures that redelivered messages (identified by eventId) are not processed again, maintaining consistency.

5. Manual Offset Management:
If your consumer uses manual offset commits for fine-grained control, there’s a risk of committing offsets too late or too early, leading to either missed or duplicate messages.
For instance, processing a message and then failing to commit the offset correctly could cause Kafka to redeliver the message.
Why Idempotency?: By checking the eventId against a Redis store, the consumer can safely handle redelivered messages without causing duplicate side effects.

6. Exactly-Once Semantics Not Fully Guaranteed:
While Kafka supports exactly-once semantics (EOS) for certain scenarios (e.g., transactional producers and consumers with isolation.level=read_committed), enabling EOS can introduce complexity and performance overhead, and it’s not always practical for all use cases.
Many applications opt for at-least-once delivery with idempotency at the consumer to achieve effectively exactly-once processing without relying on Kafka’s transactional features.
Why Idempotency?: Idempotency at the consumer layer (via EventHandlerInterceptor) is a simpler and more flexible way to ensure no duplicate processing, especially for applications where EOS is not configured.

7. Business Logic Requirements:
Certain business operations (e.g., financial transactions, order processing, or inventory updates) require strict guarantees that an event is processed exactly once to avoid errors like double-charging a customer or overselling inventory.
For example, a DomainEvent representing an order creation must not create multiple orders if the same event is delivered multiple times.
Why Idempotency?: The eventId in your DomainEvent allows the consumer to detect duplicates and ensure the business operation is executed only once.

### Error handling

#### Error handling - Producer side

Several issues can arise during the message publishing process, threatening data accuracy and potentially leading to data loss. Understanding these potential points of failure is the first step in building robust solutions.
- Invalid Data: The source system might provide data in an incorrect format or with invalid content, which the producer cannot process.
- Kafka Broker Unavailability: The producer may be unable to connect to a broker due to network issues, broker downtime, or other connectivity problems.
- Producer Interruption: If the producer application crashes or is interrupted before a message is successfully sent, data residing in the producer’s memory might be lost.
- Topic Not Found: The producer might attempt to send data to a topic that doesn’t exist.
- Exceptions During Data Processing: Unexpected errors can occur during data serialization, message construction, or other processing steps within the producer.

These issues can be broadly categorized as:
1. **Retriable Errors**: Errors that have a chance of being resolved by retrying the operation. Examples include network glitches, temporary broker unavailability, and `NotEnoughReplicasException`.
2. **Non-Retriable Errors**: Errors that retrying will not fix. These often relate to invalid data or configuration issues (e.g., INVALID_CONFIG exceptions).

##### Handling Retriable Errors: Ensuring Message Delivery

Retries are essential for handling transient errors and ensuring reliable message delivery. They are a cornerstone of building fault-tolerant systems.
- Handling Transient Errors: Temporary network issues, broker restarts, leader elections, and `NotEnoughReplicasException` can all cause temporary disruptions. Retries allow the producer to weather these storms without losing data.
- Preventing Data Loss: Without retries, messages affected by transient errors would be lost. Automatic retries ensure that messages are eventually delivered.
- Ensuring Eventual Delivery: The producer will keep attempting to resend a message until it succeeds or a defined timeout is reached.

1. Enable Idempotence (Recommended)

To avoid duplicates during retries, enable idempotence:

```properties
enable.idempotence=true
```

- Ensures exactly-once delivery semantics per partition.
- Automatically sets:
  - `acks=all`
  - `retries=Integer.MAX_VALUE`
  - `max.in.flight.requests.per.connection <= 5`

2. Configure Retries and Backoff

If a transient failure occurs (e.g., broker is temporarily unavailable), Kafka can retry automatically.

```properties
retries=5 # Number of times Kafka will retry sending the record.
retry.backoff.ms=1000 ## Delay between retries.
```

Without idempotence, retries may cause duplicate messages if the broker receives the message but the acknowledgment is lost.

3. Use Proper Acknowledgment Level

```properties
acks=all
```

- `acks=0`: Fire and forget (no durability).
- `acks=1`: Leader only ack (risk of data loss if leader fails).
- `acks=all`: All replicas must ack — safest but may increase latency.

##### Handling Non-Retriable Errors: Application-Level Error Handling

Non-retriable errors require a different approach. Since retrying won’t solve the problem, you need to implement error handling at the application level. This typically involves using a callback mechanism with the producer.send() method.

Example (Java with Callback): Demonstrates how to use callbacks to log retriable exceptions and route non-retriable exceptions to a Dead-Letter Queue (DLQ).

```java
public <T> void send(T message, Optional<String> topicName) {
    Objects.requireNonNull(message, "message cannot be null");
    String targetTopic = topicName.orElse(kafkaProperties.getTopicName());
    log.info("Publishing message to topic: {}", targetTopic);

    try {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                targetTopic, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                onSuccess(result, message);
            } else {
                handleException(ex, message, targetTopic);
            }
        });
    } catch (Exception ex) {
        log.error("Exception occurred while sending message to Kafka", ex);
        throw new PublisherException("Failed to publish message to Kafka", ex);
    }
}

private <T> void onSuccess(final SendResult<String, Object> result, final T t) {
    log.info("Successfully send message=[{}] to topic-partition={}-{} with offset={}",
            t,
            result.getRecordMetadata().topic(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
}

private <T> void handleException(Throwable ex, final T payload, final String targetTopic) {
    if (ex instanceof UnsupportedVersionException
            || ex instanceof RecordTooLargeException
            || ex instanceof CorruptRecordException) {
        log.error("Non-Retriable exception occurred. Sending message to dead-letter queue: [{}] Error: {}", payload, ex.getMessage());
        sendToDeadLetterTopic(payload, targetTopic);
    } else if (ex instanceof SerializationException) {
        log.error("Serialization error! message=[{}]", payload);
    } else if (ex instanceof RetriableException) {
        log.warn("Retriable exception occurred. Kafka will retry automatically: {}", ex.getMessage());
    }
}

private <T> void sendToDeadLetterTopic(T payload, final String topic) {
    String deadLetterTopic = topic + ".DLT";
    log.info("Sending failed message to Dead Letter Topic: {}", deadLetterTopic);
    kafkaTemplate.send(deadLetterTopic, payload);
}
```

#### Error handling - Consumer side

##### Spring’s Retryable Topics: Non-Blocking Retries for the Win

Spring Kafka offers a clever way to handle retries: **retryable topics**. Instead of blocking the main consumer thread while retrying, Spring sends the failed message to a separate retry topic. This keeps the main consumer moving, ensuring that other messages aren’t held up by one stubborn one.

It’s like having a dedicated team of mail carriers for those tricky letters. If the first team can’t figure it out, they pass it to a second team, and so on.

You can configure how many retry topics you want, with increasing backoff times between retries. This gives your system time to recover from transient issues.

To use retryable topics, just add `@RetryableTopic` to your `@KafkaListener` and configure it to your liking.

```java
@RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 5000, multiplier = 1.5, maxDelay = 15000),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        dltTopicSuffix = ".DLT",
        exclude = {DeserializationException.class,
                MessageConversionException.class,
                ConversionException.class,
                MethodArgumentResolutionException.class,
                NoSuchMethodException.class,
                ClassCastException.class}
)
@KafkaListener(topics = "${topic-name}",
        containerFactory = "defaultKafkaListenerContainerFactory")
public void consumeEvents(User user,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.OFFSET) long offset) {

}
```

If you prefer a global configuration, I have good news for you. if you want to access all the re-tryable configurations in one class and define all conditions for all of your `@KafkaListener`, then you can create a `@Configuration` class and define a bean for `RetryTopicConfiguration`.

```java
@Bean
public RetryTopicConfiguration myRetryTopic(KafkaTemplate<String, MyPojo> template) {
    return RetryTopicConfigurationBuilder
            .newInstance()
            .fixedBackOff(3000)
            .maxAttempts(5)
            .concurrency(1)
            .includeTopics(List.of("my-topic", "my-other-topic"))
            .create(template);
}

@Bean
public RetryTopicConfiguration myOtherRetryTopic(KafkaTemplate<String, MyOtherPojo> template) {
    return RetryTopicConfigurationBuilder
            .newInstance()
            .exponentialBackoff(1000, 2, 5000)
            .maxAttempts(4)
            .excludeTopics(List.of("my-topic", "my-other-topic"))
            .retryOn(MyException.class)
            .create(template);
}
```

Once all retries are exhausted, the message lands in the DLQ. To process these messages, just create a method with `@DltHandler`.

```java
@RetryableTopic
@KafkaListener(topics = "my-annotated-topic")
public void processMessage(MyPojo message) {
    // ... message processing
}

@DltHandler
public void processDltMessage(MyPojo message) {
    // ... message processing, persistence, etc
}
```

##### Bonus: Blocking Retries (For When You’re Feeling Impatient)

Sometimes, you want to try retrying a message a few times in the same connection before sending it to a retry topic. This is like giving that weird letter a quick once-over before sending it to the special team.

You can configure blocking retries by overriding configureBlockingRetries in a `@Configuration` class that extends `RetryTopicConfigurationSupport`.

```java
@Configuration
public class RetryConfig extends RetryTopicConfigurationSupport {

    @Override
    protected void configureBlockingRetries(BlockingRetriesConfigurer configurer) {
        configurer.retryOn(ConsumerException.class)
                .backOff(new FixedBackOff(1_000, 3));
    }
}
```

##### Custom Error Handlers: Taking Control of the Chaos

For ultimate control, you can create a custom error handler. This allows you to define your own retry strategies, send messages to the DLQ, and commit offsets for failed messages.

Create a bean that creates an instance of DefaultErrorHandler. This class allows you to

✔ Retry messages before failing completely
✔ Send failed messages to a Dead Letter Topic (DLT)
✔ Commit offsets for failed messages to avoid infinite retries
✔ Prevent blocking the consumer when an error occurs

```java
@Bean
public DefaultErrorHandler customErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    int attempts = 3;
    BackOff backOff = new FixedBackOff(2000, attempts);

    // Dead Letter Publishing Recoverer: Sends messages to DLT after max retries
    final DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (consumerRecord, ex) -> {
                log.error(" Received: {} , after {} attempts, exception: {}",
                        consumerRecord.value(), attempts, ex.getMessage());
                return new TopicPartition(consumerRecord.topic() + ".DLT", consumerRecord.partition());
            });

    DefaultErrorHandler customErrorHandler = new DefaultErrorHandler(recoverer, backOff);

    // Configure retryable and non-retryable exceptions
    customErrorHandler.addRetryableExceptions(IOException.class, ConsumerException.class);
    customErrorHandler.addNotRetryableExceptions(NullPointerException.class);

    // Set error handling behavior
    customErrorHandler.setCommitRecovered(true); // Ensures committed offsets for failed records

    return customErrorHandler;
}
```

Then, inject it (`DefaultErrorHandler`) into your `KafkaListenerContainerFactory`:

```java
// (Use AckMode.MANUAL_IMMEDIATE and you should manually call ack mode for successful processing in your kafka Listener)
@Bean
public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> kafkaCustomErrorRetryContainerFactory(ConsumerFactory<String, Object> consumerFactory, DefaultErrorHandler defaultErrorHandler) {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConcurrency(1);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    factory.setCommonErrorHandler(defaultErrorHandler);
    factory.setConsumerFactory(consumerFactory);
    return factory;
}
```

---

### Performance tuning

Useful configs for producer's performance:

```properties
linger.ms=10               # Wait before batching
batch.size=16384           # Batch size
buffer.memory=33554432     # Total buffer size
max.block.ms=60000         # Time producer will block if buffer is full
```

If `max.block.ms` is too small and the buffer is full, you'll get a `TimeoutException`.
