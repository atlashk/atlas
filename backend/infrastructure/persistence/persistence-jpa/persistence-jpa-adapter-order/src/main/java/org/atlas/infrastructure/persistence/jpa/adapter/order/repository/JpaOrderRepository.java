package org.atlas.infrastructure.persistence.jpa.adapter.order.repository;

import java.util.Optional;
import org.atlas.domain.order.repository.FindOrderCriteria;
import org.atlas.infrastructure.persistence.jpa.adapter.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaBaseRepository<JpaOrderEntity, Integer> {

  @Query("""
      select o
      from JpaOrderEntity o
      left join fetch o.orderItems
      where 1 = 1
        and (:#{#criteria.id} is null or o.id = :#{#criteria.id})
        and (:#{#criteria.userId} is null or o.userId = :#{#criteria.userId})
        and (:#{#criteria.status} is null or o.status = :#{#criteria.status})
        and (:#{#criteria.startDate} is null or date(o.createdAt) >= :#{#criteria.startDate})
        and (:#{#criteria.endDate} is null or date(o.createdAt) >= :#{#criteria.endDate})
      """)
  Page<JpaOrderEntity> findByCriteria(@Param("criteria") FindOrderCriteria criteria, Pageable pageable);

  @Query("""
      select o
      from JpaOrderEntity o
      left join fetch o.orderItems
      where o.id = :id
      """)
  Optional<JpaOrderEntity> findByIdAndFetch(@Param("id") Integer id);
}
