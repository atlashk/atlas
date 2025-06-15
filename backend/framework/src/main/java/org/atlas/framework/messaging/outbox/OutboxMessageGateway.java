package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.MessageGateway;

@RequiredArgsConstructor
@Slf4j
public class OutboxMessageGateway implements MessageGateway {

  private final OutboxMessageRepository outboxMessageRepository;

  @Override
  public void send(Object messagePayload, String messageKey, String destination) {
    OutboxMessageEntity outboxMessage = newOutboxMessage(messagePayload, messageKey, destination);
    outboxMessageRepository.insert(outboxMessage);
    log.info("Inserted outbox message {}", outboxMessage);
  }

  private OutboxMessageEntity newOutboxMessage(Object messagePayload, String messageKey,
      String destination) {
    OutboxMessageEntity outboxMessage = new OutboxMessageEntity();
    outboxMessage.setMessagePayload(JsonUtil.getInstance().toJson(messagePayload));
    outboxMessage.setMessageClass(messagePayload.getClass().getName());
    outboxMessage.setMessageKey(messageKey);
    outboxMessage.setDestination(destination);
    outboxMessage.setStatus(OutboxMessageStatus.PENDING);
    outboxMessage.setRetries(0);
    return outboxMessage;
  }
}
