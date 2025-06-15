package org.atlas.infrastructure.persistence.jpa.adapter.user.repository;

import java.util.Optional;
import org.atlas.domain.user.repository.FindUserCriteria;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaBaseRepository<JpaUserEntity, Integer> {

  @Query("""
    select u
    from JpaUserEntity u
    where 1 = 1
      and (:#{#criteria.id} is null or u.id = :#{#criteria.id})
      and (:#{#criteria.keyword} is null or (
        lower(u.username) like lower(concat('%', :#{#criteria.keyword}, '%'))
        or lower(u.email) like lower(concat('%', :#{#criteria.keyword}, '%'))
        or u.phoneNumber like concat('%', :#{#criteria.keyword}, '%')
      ))
      and (:#{#criteria.role} is null or u.role = :#{#criteria.role})
    """)
  Page<JpaUserEntity> findByCriteria(@Param("criteria") FindUserCriteria criteria, Pageable pageable);

  Optional<JpaUserEntity> findByUsername(String username);

  Optional<JpaUserEntity> findByEmail(String email);

  Optional<JpaUserEntity> findByPhoneNumber(String phoneNumber);
}
