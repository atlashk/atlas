package org.atlas.infrastructure.persistence.jpa.impl.user.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaUser;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUser, Integer> {

  Optional<JpaUser> findByUsername(String username);

  Optional<JpaUser> findByEmail(String email);

  Optional<JpaUser> findByPhoneNumber(String phoneNumber);
}
