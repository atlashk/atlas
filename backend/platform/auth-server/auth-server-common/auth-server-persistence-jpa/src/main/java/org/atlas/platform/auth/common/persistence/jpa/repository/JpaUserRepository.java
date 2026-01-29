package org.atlas.platform.auth.common.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.platform.auth.common.persistence.jpa.entity.JpaUser;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUser, Integer> {

  Optional<JpaUser> findByUsername(String username);

  Optional<JpaUser> findByEmail(String email);

  Optional<JpaUser> findByPhoneNumber(String phoneNumber);
}
