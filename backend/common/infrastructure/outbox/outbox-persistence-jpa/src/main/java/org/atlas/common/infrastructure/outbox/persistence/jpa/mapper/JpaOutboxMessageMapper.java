package org.atlas.common.infrastructure.outbox.persistence.jpa.mapper;

import org.atlas.common.framework.messaging.outbox.OutboxMessage;
import org.atlas.common.infrastructure.outbox.persistence.jpa.entity.JpaOutboxMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaOutboxMessageMapper {

  JpaOutboxMessageMapper INSTANCE = Mappers.getMapper(JpaOutboxMessageMapper.class);

  OutboxMessage toOutboxMessage(JpaOutboxMessage jpaOutboxMessage);

  JpaOutboxMessage toJpaOutboxMessage(OutboxMessage outboxMessage);
}
