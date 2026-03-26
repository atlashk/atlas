package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaConversationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaConversationRepository extends JpaBaseRepository<JpaConversationEntity, String> {

  List<JpaConversationEntity> findByUserId(String userId, Pageable pageable);

  List<JpaConversationEntity> findByUserId(String userId, Sort sort);

  List<JpaConversationEntity> findByUserId(String userId);
}
