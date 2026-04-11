package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversation;

public interface ConversationRepository {

  List<ChatConversation> findByUserId(String userId, PagingRequest pagingRequest);

  void insert(ChatConversation conversation);

  void delete(String conversationId);
}
