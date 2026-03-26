package org.atlas.services.catalog.port.out.repository.chatbot;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ConversationEntity;

public interface ConversationRepository {

  List<ConversationEntity> findByUserId(String userId, PagingRequest pagingRequest);

  void insert(ConversationEntity conversation);

  void delete(String conversationId);
}
