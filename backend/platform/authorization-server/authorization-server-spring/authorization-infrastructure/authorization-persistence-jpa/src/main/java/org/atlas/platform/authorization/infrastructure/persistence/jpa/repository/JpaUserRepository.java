package org.atlas.platform.authorization.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUserEntity, String> {

  Optional<JpaUserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);
}
