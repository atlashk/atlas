package org.atlas.services.catalog.port.in.chatbot.service;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversationEntity;
import org.atlas.services.catalog.port.in.chatbot.model.ChatSendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;

public interface ChatConversationService {

  List<ChatConversationEntity> retrieveConversationList(PagingRequest pagingRequest);

  SendMessageOutput startConversation(ChatSendMessageInput input);

  void deleteConversation(String conversationId);
}
