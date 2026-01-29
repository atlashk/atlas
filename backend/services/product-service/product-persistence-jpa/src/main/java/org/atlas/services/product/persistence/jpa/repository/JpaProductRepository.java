package org.atlas.services.product.persistence.jpa.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.persistence.jpa.entity.JpaProduct;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaBaseRepository<JpaProduct, Integer> {

  @Query("""
        select p
        from JpaProduct p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id in (:ids)
      """)
  List<JpaProduct> findAllByIdInWithAssociations(@Param("ids") List<Integer> ids);

  @Query("""
        select p
        from JpaProduct p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id = :id
      """)
  Optional<JpaProduct> findByIdWithAssociations(@Param("id") Integer id);

  @Query("""
        select p
        from JpaProduct p
        where p.id = :id
      """)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<JpaProduct> findByIdWithLock(@Param("id") Integer id);

  @Modifying
  @Query("""
      update JpaProduct p
      set p.quantity = p.quantity - :decrement
      where p.id = :id
      and p.quantity >= :decrement
      """)
  int decreaseQuantityWithConstraint(@Param("id") Integer id,
      @Param("decrement") Integer decrement);

  @Modifying
  @Query("""
      update JpaProduct p
      set p.quantity = p.quantity + :increment
      where p.id = :id
      """)
  int increaseQuantity(@Param("id") Integer id, @Param("increment") Integer increment);
}
