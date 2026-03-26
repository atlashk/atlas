package org.atlas.services.catalog.api.rest.chatbot.mapper;

import org.atlas.services.catalog.api.rest.chatbot.model.MessageResponse;
import org.atlas.services.catalog.api.rest.chatbot.model.SendMessageRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendMessageResponse;
import org.atlas.services.catalog.domain.entity.chatbot.MessageEntity;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

  MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

  MessageResponse toMessageResponse(MessageEntity message);

  SendMessageInput toSendMessageInput(SendMessageRequest request);

  SendMessageResponse toSendMessageResponse(SendMessageOutput output);
}
