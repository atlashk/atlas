package org.atlas.libs.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.json.JsonUtil;
import org.atlas.libs.framework.messaging.publisher.Message;
import org.atlas.libs.framework.messaging.publisher.MessagePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(OutboxMessageRepository.class)
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageService {

  private static final int MAX_RETRIES = 3;

  private final OutboxMessageRepository outboxMessageRepository;
  private final MessagePublisher messagePublisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processOutboxMessage(OutboxMessage outboxMessage) {
    try {
      Message message = JsonUtil.getInstance()
          .toObject(outboxMessage.getMessage(), Message.class);
      messagePublisher.publish(message);

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
