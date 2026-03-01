package org.atlas.libs.outbox.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.libs.outbox.persistence.jpa.entity.JpaOutboxMessageEntity;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxMessageRepository extends
    JpaBaseRepository<JpaOutboxMessageEntity, Integer> {

  List<JpaOutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status);
}
