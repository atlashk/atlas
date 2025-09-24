package org.atlas.infrastructure.messaging.external.outbox.core;

import java.util.List;

public interface OutboxMessageRepository {

  List<OutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);

  void insert(OutboxMessageEntity outboxMessageEntity);

  void update(OutboxMessageEntity outboxMessageEntity);
}
