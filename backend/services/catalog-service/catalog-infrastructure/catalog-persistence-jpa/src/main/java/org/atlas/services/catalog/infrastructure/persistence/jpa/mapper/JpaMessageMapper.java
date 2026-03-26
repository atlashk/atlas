package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.chatbot.MessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaMessageEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaMessageMapper {

  JpaMessageMapper INSTANCE = Mappers.getMapper(JpaMessageMapper.class);

  JpaMessageEntity toJpaMessage(MessageEntity message);

  MessageEntity toMessage(JpaMessageEntity jpaMessage);
}
