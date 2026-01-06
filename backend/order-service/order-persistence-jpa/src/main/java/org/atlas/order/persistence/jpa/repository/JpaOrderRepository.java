package org.atlas.order.persistence.jpa.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.atlas.order.application.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.common.framework.domain.order.OrderStatus;
import org.atlas.order.persistence.jpa.entity.JpaOrder;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
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

  @Query("""
        select new org.atlas.order.application.admin.model.AdminMonthlyOrderAggregation(
          year(o.createdAt), month(o.createdAt), coalesce(sum(o.amount), 0)
        )
        from JpaOrder o
        where o.status = :status
        group by year(o.createdAt), month(o.createdAt)
        order by year(o.createdAt), month(o.createdAt)
      """)
  List<AdminMonthlyOrderAggregation> aggregateMonthlyByStatus(@Param("status") OrderStatus status);
}
