package org.atlas.framework.messaging.gateway.outbox;

import java.util.List;

public interface OutboxMessageRepository {

  List<OutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);

  void insert(OutboxMessageEntity outboxMessageEntity);

  void update(OutboxMessageEntity outboxMessageEntity);
}
