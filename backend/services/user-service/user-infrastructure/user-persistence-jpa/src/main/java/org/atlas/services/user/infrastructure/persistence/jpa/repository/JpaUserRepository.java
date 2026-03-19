package org.atlas.services.user.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUserEntity, String> {

  Optional<JpaUserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);
}
