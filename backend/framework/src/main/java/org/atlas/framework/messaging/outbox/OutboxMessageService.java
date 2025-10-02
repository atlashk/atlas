package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageService {

  private static final int MAX_RETRIES = 3;

  private final OutboxMessageRepository outboxMessageRepository;
  private final MessagePublisherPort messagePublisherPort;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processOutboxMessage(OutboxMessageEntity outboxMessage) {
    try {
      Class<?> messageClass = Class.forName(outboxMessage.getMessageClass());
      Object messagePayload = JsonUtil.getInstance()
          .toObject(outboxMessage.getMessagePayload(), messageClass);

      messagePublisherPort.doPublish(messagePayload,
          outboxMessage.getMessageKey(),
          outboxMessage.getDestination());

      outboxMessage.markAsProcessed();
    } catch (Exception e) {
      log.error("Failed to process outbox message {}", outboxMessage, e);
      outboxMessage.setError(e.getMessage());

      if (outboxMessage.getRetries() >= MAX_RETRIES) {
        outboxMessage.setStatus(OutboxMessageStatus.FAILED);
      } else {
        outboxMessage.incRetries();
      }
    }
    outboxMessageRepository.update(outboxMessage);
  }
}
