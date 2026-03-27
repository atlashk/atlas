package org.atlas.services.catalog.infrastructure.persistence.jpa.repository.chatbot;

import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatConversationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaChatConversationRepository extends
    JpaBaseRepository<JpaChatConversationEntity, String> {

  List<JpaChatConversationEntity> findByUserId(String userId, Pageable pageable);
}
