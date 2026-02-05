package org.atlas.services.product.infrastructure.persistence.jpa.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProduct;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaBaseRepository<JpaProduct, String> {

  @Query("""
        select p
        from JpaProduct p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.productId in (:productIds)
      """)
  List<JpaProduct> findAllByProductIdInWithAssociations(@Param("productIds") List<String> productIds);

  @Query("""
        select p
        from JpaProduct p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.productId = :productId
      """)
  Optional<JpaProduct> findByProductIdWithAssociations(@Param("productId") String productId);

  @Query("""
        select p
        from JpaProduct p
        where p.productId = :productId
      """)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<JpaProduct> findByProductIdWithLock(@Param("productId") String productId);

  @Modifying
  @Query("""
      update JpaProduct p
      set p.quantity = p.quantity - :decrement
      where p.productId = :productId
      and p.quantity >= :decrement
      """)
  int decreaseQuantityWithConstraint(@Param("productId") String productId,
      @Param("decrement") Integer decrement);

  @Modifying
  @Query("""
      update JpaProduct p
      set p.quantity = p.quantity + :increment
      where p.productId = :productId
      """)
  int increaseQuantity(@Param("productId") String productId, @Param("increment") Integer increment);
}
