package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.chatbot.ConversationEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaConversationEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaConversationMapper {

  JpaConversationMapper INSTANCE = Mappers.getMapper(JpaConversationMapper.class);

  JpaConversationEntity toJpaConversation(ConversationEntity conversation);

  ConversationEntity toConversation(JpaConversationEntity jpaConversation);
}
