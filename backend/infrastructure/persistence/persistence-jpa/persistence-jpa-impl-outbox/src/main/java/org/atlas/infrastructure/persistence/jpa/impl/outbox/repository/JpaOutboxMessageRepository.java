package org.atlas.infrastructure.persistence.jpa.impl.outbox.repository;

import java.util.List;
import org.atlas.infrastructure.messaging.outbox.core.OutboxMessageStatus;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends JpaRepository<JpaOutboxMessageEntity, Long> {

  List<JpaOutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
