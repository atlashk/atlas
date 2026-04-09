package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.chatbot;

import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatMessageEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaChatMessageMapper {

  JpaChatMessageMapper INSTANCE = Mappers.getMapper(JpaChatMessageMapper.class);

  JpaChatMessageEntity toJpaMessage(ChatMessageEntity message);

  ChatMessageEntity toMessage(JpaChatMessageEntity jpaMessage);
}
