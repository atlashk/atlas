package org.atlas.infrastructure.persistence.jpa.adapter.outbox.repository;

import java.util.List;
import org.atlas.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.infrastructure.persistence.jpa.adapter.outbox.entity.JpaOutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends JpaRepository<JpaOutboxMessageEntity, Long> {

  List<JpaOutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
