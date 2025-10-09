package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageService {

  private static final int MAX_RETRIES = 3;

  private final OutboxMessageRepository outboxMessageRepository;
  private final MessagePublisher messagePublisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processOutboxMessage(OutboxMessageEntity outboxMessage) {
    try {
      PublishRequest publishRequest = JsonUtil.getInstance()
          .toObject(outboxMessage.getPublishRequest(), PublishRequest.class);
      messagePublisher.publish(publishRequest);

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
