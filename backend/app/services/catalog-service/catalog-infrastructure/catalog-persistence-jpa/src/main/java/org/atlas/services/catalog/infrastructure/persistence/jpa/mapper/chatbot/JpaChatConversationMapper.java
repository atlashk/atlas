package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.chatbot;

import org.atlas.services.catalog.domain.entity.chatbot.ChatConversation;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatConversationEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaChatConversationMapper {

  JpaChatConversationMapper INSTANCE = Mappers.getMapper(JpaChatConversationMapper.class);

  JpaChatConversationEntity toJpaConversation(ChatConversation conversation);

  ChatConversation toConversation(JpaChatConversationEntity jpaConversation);
}
