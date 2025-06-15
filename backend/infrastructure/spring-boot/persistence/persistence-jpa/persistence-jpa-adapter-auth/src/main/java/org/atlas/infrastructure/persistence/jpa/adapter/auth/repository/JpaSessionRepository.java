package org.atlas.infrastructure.persistence.jpa.adapter.auth.repository;

import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaSessionEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSessionRepository extends JpaBaseRepository<JpaSessionEntity, String> {
}
