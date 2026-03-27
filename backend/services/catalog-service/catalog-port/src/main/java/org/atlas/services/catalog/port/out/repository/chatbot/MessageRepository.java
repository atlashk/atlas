package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;

public interface MessageRepository {

  List<ChatMessageEntity> findByConversationId(String conversationId, PagingRequest pagingRequest);

  void insert(ChatMessageEntity chatMessage);

  void deleteByConversationId(String conversationId);
}
