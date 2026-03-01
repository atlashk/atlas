package org.atlas.libs.framework.messaging.outbox;

import java.util.List;

public interface OutboxMessageRepository {

  List<OutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);

  void insert(OutboxMessageEntity outboxMessage);

  void update(OutboxMessageEntity outboxMessage);
}
