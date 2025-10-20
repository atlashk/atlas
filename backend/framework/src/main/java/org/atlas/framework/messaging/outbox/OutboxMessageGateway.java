package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.Message;
import org.atlas.framework.messaging.publisher.MessageGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(OutboxMessageRepository.class)
@ConditionalOnProperty(value = "app.messaging.gateway", havingValue = "outbox")
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageGateway implements MessageGateway {

  private final OutboxMessageRepository outboxMessageRepository;

  @Override
  public void sendMessage(Message message) {
    OutboxMessageEntity outboxMessage = OutboxMessageEntity.builder()
        .message(JsonUtil.getInstance().toJson(message))
        .status(OutboxMessageStatus.PENDING)
        .retries(0)
        .build();
    outboxMessageRepository.insert(outboxMessage);
  }
}
