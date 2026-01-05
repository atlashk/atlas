package org.atlas.common.framework.messaging.outbox;

import java.util.List;

public interface OutboxMessageRepository {

  List<OutboxMessage> findByStatusOrderByCreatedAt(OutboxMessageStatus status);

  void insert(OutboxMessage outboxMessage);

  void update(OutboxMessage outboxMessage);
}
