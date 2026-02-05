package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCartRepository extends JpaBaseRepository<JpaCartEntity, Integer> {

  @Query("""
      select c
      from JpaCartEntity c
      left join fetch c.cartItems
      where c.userId = :userId
      """)
  Optional<JpaCartEntity> findByUserIdAndFetch(@Param("userId") String userId);
}
