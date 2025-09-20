package org.atlas.infrastructure.persistence.jpa.impl.user.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaUserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUserEntity, Integer> {

  Optional<JpaUserEntity> findByUsername(String username);

  Optional<JpaUserEntity> findByEmail(String email);

  Optional<JpaUserEntity> findByPhoneNumber(String phoneNumber);
}
