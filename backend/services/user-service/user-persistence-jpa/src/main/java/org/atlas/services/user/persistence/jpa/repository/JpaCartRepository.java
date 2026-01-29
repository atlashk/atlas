package org.atlas.services.user.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.user.persistence.jpa.entity.JpaCart;
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
