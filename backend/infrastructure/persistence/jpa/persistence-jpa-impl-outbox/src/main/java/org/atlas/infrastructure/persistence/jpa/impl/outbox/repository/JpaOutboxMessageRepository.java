package org.atlas.infrastructure.persistence.jpa.impl.outbox.repository;

import java.util.List;
import org.atlas.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessage;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends
    JpaBaseRepository<JpaOutboxMessage, Integer> {

  List<JpaOutboxMessage> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
