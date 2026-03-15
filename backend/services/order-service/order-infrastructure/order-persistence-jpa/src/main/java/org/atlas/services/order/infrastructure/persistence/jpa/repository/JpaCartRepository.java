package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartItemEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCartRepository extends JpaBaseRepository<JpaCartItemEntity, Integer> {

  @Query("""
      select ci
      from JpaCartItemEntity ci
      where ci.userId = :userId
      """)
  List<JpaCartItemEntity> findByUserId(@Param("userId") String userId);

  @Modifying
  @Query(value = """
      insert into cart_item (user_id, product_id, quantity, created_at, updated_at)
      values (:userId, :productId, :quantity, now(), now())
      on duplicate key update
        quantity = quantity + values(quantity),
        updated_at = now()
      """, nativeQuery = true)
  void upsertMySql(
      @Param("userId") String userId,
      @Param("productId") String productId,
      @Param("quantity") Integer quantity
  );

  @Modifying
  @Query(value = """
      insert into cart_item (user_id, product_id, quantity, created_at, updated_at)
      values (:userId, :productId, :quantity, current_timestamp, current_timestamp)
      on conflict (user_id, product_id) do update set
        quantity = cart_item.quantity + excluded.quantity,
        updated_at = current_timestamp
      """, nativeQuery = true)
  void upsertPostgres(
      @Param("userId") String userId,
      @Param("productId") String productId,
      @Param("quantity") Integer quantity
  );

  @Modifying
  @Query("""
      update JpaCartItemEntity ci
      set ci.quantity = :quantity
      where ci.userId = :userId
        and ci.productId = :productId
      """)
  void updateQuantity(
      @Param("userId") String userId,
      @Param("productId") String productId,
      @Param("quantity") Integer quantity
  );

  @Modifying
  @Query("""
      delete from JpaCartItemEntity ci
      where ci.userId = :userId
        and ci.productId = :productId
      """)
  void deleteByUserIdAndProductId(
      @Param("userId") String userId,
      @Param("productId") String productId
  );

  @Modifying
  @Query("""
      delete from JpaCartItemEntity ci
      where ci.userId = :userId
      """)
  void deleteByUserId(@Param("userId") String userId);
}
