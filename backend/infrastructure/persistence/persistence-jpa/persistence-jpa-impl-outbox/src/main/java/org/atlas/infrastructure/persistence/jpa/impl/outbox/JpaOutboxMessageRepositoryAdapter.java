package org.atlas.infrastructure.persistence.jpa.impl.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.messaging.outbox.core.OutboxMessageEntity;
import org.atlas.infrastructure.messaging.outbox.core.OutboxMessageRepository;
import org.atlas.infrastructure.messaging.outbox.core.OutboxMessageStatus;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessageEntity;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.repository.JpaOutboxMessageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaOutboxMessageRepositoryAdapter implements OutboxMessageRepository {

  private final JpaOutboxMessageRepository jpaOutboxMessageRepository;

  @Override
  public List<OutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status) {
    List<JpaOutboxMessageEntity> jpaOutboxMessageEntities =
        jpaOutboxMessageRepository.findByStatusOrderByCreatedAt(status);
    return ObjectMapperUtil.getInstance()
        .mapList(jpaOutboxMessageEntities, OutboxMessageEntity.class);
  }

  @Override
  public void insert(OutboxMessageEntity outboxMessageEntity) {
    JpaOutboxMessageEntity jpaOutboxMessageEntity = ObjectMapperUtil.getInstance()
        .map(outboxMessageEntity, JpaOutboxMessageEntity.class);
    jpaOutboxMessageRepository.save(jpaOutboxMessageEntity);
    outboxMessageEntity.setId(jpaOutboxMessageEntity.getId());
  }

  @Override
  public void update(OutboxMessageEntity outboxMessageEntity) {
    JpaOutboxMessageEntity jpaOutboxMessageEntity = ObjectMapperUtil.getInstance()
        .map(outboxMessageEntity, JpaOutboxMessageEntity.class);
    jpaOutboxMessageRepository.save(jpaOutboxMessageEntity);
  }
}
