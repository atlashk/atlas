package org.atlas.infrastructure.persistence.jpa.impl.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.messaging.outbox.OutboxMessageEntity;
import org.atlas.framework.messaging.outbox.OutboxMessageRepository;
import org.atlas.framework.messaging.outbox.OutboxMessageStatus;
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
  public void insert(OutboxMessageEntity outboxMessage) {
    JpaOutboxMessageEntity jpaOutboxMessage = ObjectMapperUtil.getInstance()
        .map(outboxMessage, JpaOutboxMessageEntity.class);
    jpaOutboxMessageRepository.insert(jpaOutboxMessage);
    outboxMessage.setId(jpaOutboxMessage.getId());
  }

  @Override
  public void update(OutboxMessageEntity outboxMessage) {
    JpaOutboxMessageEntity jpaOutboxMessage = ObjectMapperUtil.getInstance()
        .map(outboxMessage, JpaOutboxMessageEntity.class);
    jpaOutboxMessageRepository.save(jpaOutboxMessage);
  }
}
