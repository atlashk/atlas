package org.atlas.libs.outbox.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.messaging.outbox.OutboxMessageEntity;
import org.atlas.libs.framework.messaging.outbox.OutboxMessageRepository;
import org.atlas.libs.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.outbox.persistence.jpa.entity.JpaOutboxMessageEntity;
import org.atlas.libs.outbox.persistence.jpa.mapper.JpaOutboxMessageMapper;
import org.atlas.libs.outbox.persistence.jpa.repository.JpaOutboxMessageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaOutboxMessageRepositoryAdapter implements OutboxMessageRepository {

  private final JpaOutboxMessageRepository jpaOutboxMessageRepository;

  @Override
  public List<OutboxMessageEntity> findByStatusOrderByCreatedAt(OutboxMessageStatus status) {
    List<JpaOutboxMessageEntity> jpaOutboxMessages =
        jpaOutboxMessageRepository.findByStatusOrderByCreatedAt(status);
    return MapperUtil.mapList(jpaOutboxMessages,
        JpaOutboxMessageMapper.INSTANCE::toOutboxMessage);
  }

  @Override
  public void insert(OutboxMessageEntity outboxMessage) {
    JpaOutboxMessageEntity jpaOutboxMessage =
        JpaOutboxMessageMapper.INSTANCE.toJpaOutboxMessage(outboxMessage);
    jpaOutboxMessageRepository.insert(jpaOutboxMessage);
    outboxMessage.setId(jpaOutboxMessage.getId());
  }

  @Override
  public void update(OutboxMessageEntity outboxMessage) {
    JpaOutboxMessageEntity jpaOutboxMessage =
        JpaOutboxMessageMapper.INSTANCE.toJpaOutboxMessage(outboxMessage);
    jpaOutboxMessageRepository.save(jpaOutboxMessage);
  }
}
