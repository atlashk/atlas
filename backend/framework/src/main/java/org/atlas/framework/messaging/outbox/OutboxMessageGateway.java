package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.MessageGateway;
import org.atlas.framework.messaging.publisher.MessageRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.messaging.gateway", havingValue = "outbox")
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageGateway implements MessageGateway {

  private final OutboxMessageRepository outboxMessageRepository;

  @Override
  public void sendMessage(MessageRequest request) {
    OutboxMessageEntity outboxMessage = OutboxMessageEntity.builder()
        .publishRequest(JsonUtil.getInstance().toJson(request))
        .status(OutboxMessageStatus.PENDING)
        .retries(0)
        .build();
    outboxMessageRepository.insert(outboxMessage);
  }
}
