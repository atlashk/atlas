package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversationEntity;

public interface ConversationRepository {

  List<ChatConversationEntity> findByUserId(String userId, PagingRequest pagingRequest);

  void insert(ChatConversationEntity conversation);

  void delete(String conversationId);
}
