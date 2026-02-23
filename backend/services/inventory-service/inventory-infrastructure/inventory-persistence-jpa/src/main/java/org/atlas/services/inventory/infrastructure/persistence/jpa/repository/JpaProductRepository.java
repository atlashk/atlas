package org.atlas.services.inventory.infrastructure.persistence.jpa.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaBaseRepository<JpaProductEntity, String> {

  @Query("""
        select p
        from JpaProductEntity p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id in (:ids)
      """)
  List<JpaProductEntity> findAllByIdInWithAssociations(@Param("ids") List<String> ids);

  @Query("""
        select p
        from JpaProductEntity p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id = :id
      """)
  Optional<JpaProductEntity> findByIdWithAssociations(@Param("id") String id);

  @Query("""
        select p
        from JpaProductEntity p
        where p.id = :id
      """)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<JpaProductEntity> findByIdWithLock(@Param("id") String id);

  @Modifying
  @Query("""
      update JpaProductEntity p
      set p.quantity = p.quantity - :decrement
      where p.id = :id
      and p.quantity >= :decrement
      """)
  int decreaseQuantityWithConstraint(@Param("id") String id,
      @Param("decrement") Integer decrement);

  @Modifying
  @Query("""
      update JpaProductEntity p
      set p.quantity = p.quantity + :increment
      where p.id = :id
      """)
  int increaseQuantity(@Param("id") String id, @Param("increment") Integer increment);
}
