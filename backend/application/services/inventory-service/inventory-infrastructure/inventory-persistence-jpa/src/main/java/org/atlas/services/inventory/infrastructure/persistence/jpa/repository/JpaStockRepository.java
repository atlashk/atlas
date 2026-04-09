package org.atlas.services.inventory.infrastructure.persistence.jpa.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaStockEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaStockRepository extends JpaBaseRepository<JpaStockEntity, String> {

  @Query("""
        select s
        from JpaStockEntity s
        where s.productId = :productId
      """)
  Optional<JpaStockEntity> findByProductId(@Param("productId") String productId);

  @Query("""
        select s
        from JpaStockEntity s
        where s.productId = :productId
      """)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<JpaStockEntity> findByProductIdWithLock(@Param("productId") String productId);

  @Modifying
  @Query("""
      update JpaStockEntity s
      set s.availableQuantity = s.availableQuantity - :quantity,
          s.reservedQuantity = s.reservedQuantity + :quantity
      where s.productId = :productId
      and s.availableQuantity >= :quantity
      """)
  int reserveStockWithConstraint(@Param("productId") String productId, 
                                 @Param("quantity") Integer quantity);

  @Modifying
  @Query("""
      update JpaStockEntity s
      set s.availableQuantity = s.availableQuantity + :quantity,
          s.reservedQuantity = s.reservedQuantity - :quantity
      where s.productId = :productId
      """)
  int releaseStock(@Param("productId") String productId, @Param("quantity") Integer quantity);
}
