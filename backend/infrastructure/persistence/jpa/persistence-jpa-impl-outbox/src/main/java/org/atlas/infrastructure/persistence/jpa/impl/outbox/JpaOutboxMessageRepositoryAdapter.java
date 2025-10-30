package org.atlas.infrastructure.persistence.jpa.impl.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.messaging.outbox.OutboxMessage;
import org.atlas.framework.messaging.outbox.OutboxMessageRepository;
import org.atlas.framework.messaging.outbox.OutboxMessageStatus;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessage;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.mapper.JpaOutboxMessageMapper;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.repository.JpaOutboxMessageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaOutboxMessageRepositoryAdapter implements OutboxMessageRepository {

  private final JpaOutboxMessageRepository jpaOutboxMessageRepository;

  @Override
  public List<OutboxMessage> findByStatusOrderByCreatedAt(OutboxMessageStatus status) {
    List<JpaOutboxMessage> jpaOutboxMessages =
        jpaOutboxMessageRepository.findByStatusOrderByCreatedAt(status);
    return ObjectMapperUtil.mapList(jpaOutboxMessages,
        JpaOutboxMessageMapper.INSTANCE::toOutboxMessage);
  }

  @Override
  public void insert(OutboxMessage outboxMessage) {
    JpaOutboxMessage jpaOutboxMessage =
        JpaOutboxMessageMapper.INSTANCE.toJpaOutboxMessage(outboxMessage);
    jpaOutboxMessageRepository.insert(jpaOutboxMessage);
    outboxMessage.setId(jpaOutboxMessage.getId());
  }

  @Override
  public void update(OutboxMessage outboxMessage) {
    JpaOutboxMessage jpaOutboxMessage =
        JpaOutboxMessageMapper.INSTANCE.toJpaOutboxMessage(outboxMessage);
    jpaOutboxMessageRepository.save(jpaOutboxMessage);
  }
}
