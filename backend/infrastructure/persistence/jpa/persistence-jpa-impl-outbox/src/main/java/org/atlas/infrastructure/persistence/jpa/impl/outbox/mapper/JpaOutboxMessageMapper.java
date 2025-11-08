package org.atlas.infrastructure.persistence.jpa.impl.outbox.mapper;

import org.atlas.framework.messaging.outbox.OutboxMessage;
import org.atlas.infrastructure.persistence.jpa.impl.outbox.entity.JpaOutboxMessage;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface JpaOutboxMessageMapper {

  JpaOutboxMessageMapper INSTANCE = Mappers.getMapper(JpaOutboxMessageMapper.class);

  OutboxMessage toOutboxMessage(JpaOutboxMessage jpaOutboxMessage);

  JpaOutboxMessage toJpaOutboxMessage(OutboxMessage outboxMessage);
}
