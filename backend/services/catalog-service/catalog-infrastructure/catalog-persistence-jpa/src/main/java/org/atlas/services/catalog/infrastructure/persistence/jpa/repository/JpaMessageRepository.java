package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaMessageRepository extends JpaBaseRepository<JpaMessageEntity, String> {

  List<JpaMessageEntity> findByConversationId(String conversationId, Pageable pageable);

  List<JpaMessageEntity> findByConversationId(String conversationId, Sort sort);

  List<JpaMessageEntity> findByConversationId(String conversationId);

  void deleteByConversationId(String conversationId);
}
