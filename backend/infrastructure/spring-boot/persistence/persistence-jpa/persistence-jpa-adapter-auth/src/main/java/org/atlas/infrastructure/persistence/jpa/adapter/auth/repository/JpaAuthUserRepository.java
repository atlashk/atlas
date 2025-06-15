package org.atlas.infrastructure.persistence.jpa.adapter.auth.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAuthUserRepository extends JpaBaseRepository<JpaUserEntity, Integer> {

  @Query("""
        SELECT u
        FROM JpaUserEntity u
        WHERE u.username = :identifier
           OR u.email = :identifier
           OR u.phoneNumber = :identifier
      """)
  Optional<JpaUserEntity> findByIdentifier(@Param("identifier") String identifier);
}
