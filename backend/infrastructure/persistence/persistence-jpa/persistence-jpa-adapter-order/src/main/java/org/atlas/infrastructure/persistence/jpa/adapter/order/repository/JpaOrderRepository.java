package org.atlas.infrastructure.persistence.jpa.adapter.order.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.infrastructure.persistence.jpa.adapter.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaBaseRepository<JpaOrderEntity, Integer> {

  @Query("""
      select o
      from JpaOrderEntity o
      left join fetch o.orderItems
      where o.id = :id
      """)
  Optional<JpaOrderEntity> findByIdAndFetch(@Param("id") Integer id);

  @Query("""
        select coalesce(sum(o.amount), 0)
        from JpaOrderEntity o
        where o.status = :status
      """)
  BigDecimal sumAmountByStatus(@Param("status") OrderStatus status);
}
