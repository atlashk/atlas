package org.atlas.services.catalog.api.rest.chatbot.mapper;

import org.atlas.services.catalog.api.rest.chatbot.model.ChatMessageResponse;
import org.atlas.services.catalog.api.rest.chatbot.model.SendChatMessageRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendChatMessageResponse;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.port.in.chatbot.model.ChatSendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {

  ChatMessageMapper INSTANCE = Mappers.getMapper(ChatMessageMapper.class);

  ChatMessageResponse toMessageResponse(ChatMessageEntity message);

  ChatSendMessageInput toSendMessageInput(SendChatMessageRequest request);

  SendChatMessageResponse toSendMessageResponse(SendMessageOutput output);
}
