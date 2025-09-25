package org.atlas.infrastructure.persistence.jpa.impl.user.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCartRepository extends JpaBaseRepository<JpaCartEntity, Integer> {

  @Query("""
      select c
      from JpaCartEntity c
      left join fetch c.cartItems
      where c.userId = :id
      """)
  Optional<JpaCartEntity> findByUserIdAndFetch(@Param("userId") Integer userId);
}
