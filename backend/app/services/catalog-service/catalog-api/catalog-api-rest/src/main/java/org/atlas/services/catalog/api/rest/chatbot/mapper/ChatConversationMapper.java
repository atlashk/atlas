package org.atlas.services.catalog.api.rest.chatbot.mapper;

import org.atlas.services.catalog.api.rest.chatbot.model.ChatConversationResponse;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatConversationMapper {

  ChatConversationMapper INSTANCE = Mappers.getMapper(ChatConversationMapper.class);

  ChatConversationResponse toConversationResponse(ChatConversation conversation);
}
