package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.MessageEntity;

public interface MessageRepository {

  List<MessageEntity> findByConversationId(String conversationId, PagingRequest pagingRequest);

  void insert(MessageEntity messageEntity);

  void deleteByConversationId(String conversationId);
}
