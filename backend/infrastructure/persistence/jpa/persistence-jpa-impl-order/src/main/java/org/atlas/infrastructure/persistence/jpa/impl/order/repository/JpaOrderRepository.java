package org.atlas.infrastructure.persistence.jpa.impl.order.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaBaseRepository<JpaOrder, Integer> {

  @Query("""
      select o
      from JpaOrder o
      left join fetch o.orderItems
      where o.id = :id
      """)
  Optional<JpaOrder> findByIdAndFetch(@Param("id") Integer id);

  @Query("""
      select o
      from JpaOrder o
      left join fetch o.orderItems
      where o.sagaId = :sagaId
      """)
  Optional<JpaOrder> findBySagaIdAndFetch(@Param("sagaId") Integer sagaId);

  @Query("""
        select coalesce(sum(o.amount), 0)
        from JpaOrder o
        where o.status = :status
      """)
  BigDecimal sumAmountByStatus(@Param("status") OrderStatus status);
}
