package org.atlas.services.catalog.api.rest.chatbot.mapper;

import org.atlas.services.catalog.api.rest.chatbot.model.ConversationResponse;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConversationMapper {

  ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

  ConversationResponse toConversationResponse(ChatConversationEntity conversation);
}
