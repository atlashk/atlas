package org.atlas.infrastructure.persistence.jpa.impl.user.repository;

import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCart;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCartRepository extends JpaBaseRepository<JpaCart, Integer> {

  @Query("""
      select c
      from JpaCart c
      left join fetch c.cartItems
      where c.userId = :userId
      """)
  Optional<JpaCart> findByUserIdAndFetch(@Param("userId") Integer userId);
}
