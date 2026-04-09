package org.atlas.services.catalog.infrastructure.persistence.jpa.repository.chatbot;

import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaChatMessageRepository extends JpaBaseRepository<JpaChatMessageEntity, String> {

  List<JpaChatMessageEntity> findByConversationId(String conversationId, Pageable pageable);

  void deleteByConversationId(String conversationId);
}
