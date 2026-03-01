package org.atlas.libs.outbox.persistence.jpa.mapper;

import org.atlas.libs.framework.messaging.outbox.OutboxMessageEntity;
import org.atlas.libs.outbox.persistence.jpa.entity.JpaOutboxMessageEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaOutboxMessageMapper {

  JpaOutboxMessageMapper INSTANCE = Mappers.getMapper(JpaOutboxMessageMapper.class);

  OutboxMessageEntity toOutboxMessage(JpaOutboxMessageEntity jpaOutboxMessage);

  JpaOutboxMessageEntity toJpaOutboxMessage(OutboxMessageEntity outboxMessage);
}
