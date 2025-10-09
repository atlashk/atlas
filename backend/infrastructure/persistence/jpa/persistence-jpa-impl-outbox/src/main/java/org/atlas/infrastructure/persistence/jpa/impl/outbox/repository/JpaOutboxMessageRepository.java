package org.atlas.infrastructure.persistence.jpa.impl.outbox.repository;

import java.util.List;
import org.atlas.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends
    JpaBaseRepository<JpaOutboxMessageEntity, Integer> {

  List<JpaOutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
