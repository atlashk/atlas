package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessage;

public interface MessageRepository {

  List<ChatMessage> findByConversationId(String conversationId, PagingRequest pagingRequest);

  void insert(ChatMessage chatMessage);

  void deleteByConversationId(String conversationId);
}
