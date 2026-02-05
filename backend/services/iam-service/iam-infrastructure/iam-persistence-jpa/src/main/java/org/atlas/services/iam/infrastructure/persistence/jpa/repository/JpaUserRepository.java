package org.atlas.services.iam.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUserEntity, String> {

  Optional<JpaUserEntity> findByUsername(String username);

  Optional<JpaUserEntity> findByEmail(String email);

  Optional<JpaUserEntity> findByPhoneNumber(String phoneNumber);
}
