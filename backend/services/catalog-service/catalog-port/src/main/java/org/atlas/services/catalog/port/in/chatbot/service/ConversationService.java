package org.atlas.services.catalog.port.in.chatbot.service;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ConversationEntity;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;

public interface ConversationService {

  List<ConversationEntity> retrieveConversationList(PagingRequest pagingRequest);

  SendMessageOutput startConversation(SendMessageInput input);

  void deleteConversation(String conversationId);
}
