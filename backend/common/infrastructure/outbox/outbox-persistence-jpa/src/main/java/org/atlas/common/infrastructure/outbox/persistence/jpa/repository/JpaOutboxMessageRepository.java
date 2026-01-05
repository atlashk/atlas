package org.atlas.common.infrastructure.outbox.persistence.jpa.repository;

import java.util.List;
import org.atlas.common.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.common.infrastructure.outbox.persistence.jpa.entity.JpaOutboxMessage;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends
    JpaBaseRepository<JpaOutboxMessage, Integer> {

  List<JpaOutboxMessage> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
