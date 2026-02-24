package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrderEntity;
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
  Optional<JpaOrderEntity> findByIdAndFetch(@Param("id") String id);

  @Query("""
      select o
      from JpaOrderEntity o
      left join fetch o.orderItems
      where o.sagaId = :sagaId
      """)
  Optional<JpaOrderEntity> findBySagaIdAndFetch(@Param("sagaId") Integer sagaId);

  @Query("""
        select coalesce(sum(o.amount), 0)
        from JpaOrderEntity o
        where o.status = :status
      """)
  BigDecimal sumAmountByStatus(@Param("status") OrderStatus status);

  @Query("""
        select new org.atlas.services.order.port.in.admin.model.AdminMonthlyOrderAggregation(
          year(o.createdAt), month(o.createdAt), coalesce(sum(o.amount), 0)
        )
        from JpaOrderEntity o
        where o.status = :status
        group by year(o.createdAt), month(o.createdAt)
        order by year(o.createdAt), month(o.createdAt)
      """)
  List<MonthlyOrderAggregation> aggregateMonthlyByStatus(@Param("status") OrderStatus status);
}
