package org.atlas.infrastructure.persistence.jpa.impl.order.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class CustomJpaOrderRepositoryUsingJpql implements CustomJpaOrderRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaOrderEntity> findByCriteria(FindOrderCriteria criteria,
      PagingRequest pagingRequest) {
    StringBuilder sqlBuilder = new StringBuilder("""
        select distinct o
        from JpaOrderEntity o
        left join fetch o.orderItems oi
        """);

    Map<String, Object> params = new HashMap<>();
    sqlBuilder.append(buildWhereClause(criteria, params));

    // Sorting
    if (pagingRequest.hasSort()) {
      sqlBuilder.append(" order by o.").append(pagingRequest.getSortBy());
      if (pagingRequest.isSortDescending()) {
        sqlBuilder.append(" desc");
      }
    }

    String sql = sqlBuilder.toString();
    TypedQuery<JpaOrderEntity> query = entityManager.createQuery(sql, JpaOrderEntity.class);

    // Set parameters
    params.forEach(query::setParameter);

    // Paging
    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(FindOrderCriteria criteria) {
    Map<String, Object> params = new HashMap<>();
    String whereClause = buildWhereClause(criteria, params);
    String countSql = """
        select count(distinct o.id)
        from JpaOrderEntity o
        left join o.orderItems oi
        """ + whereClause;
    TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
    params.forEach(countQuery::setParameter);
    return countQuery.getSingleResult();
  }

  private String buildWhereClause(FindOrderCriteria criteria, Map<String, Object> params) {
    StringBuilder whereClauseBuilder = new StringBuilder("where 1=1 ");

    if (criteria.getOrderId() != null) {
      whereClauseBuilder.append(" and o.id = :orderId ");
      params.put("orderId", criteria.getOrderId());
    }

    if (criteria.getUserId() != null) {
      whereClauseBuilder.append(" and o.userId = :userId ");
      params.put("userId", criteria.getUserId());
    }

    if (criteria.getProductId() != null) {
      whereClauseBuilder.append(" and oi.productId = :productId ");
      params.put("productId", criteria.getProductId());
    }

    if (criteria.getStatus() != null) {
      whereClauseBuilder.append(" and o.status = :status ");
      params.put("status", criteria.getStatus());
    }

    if (criteria.getStartDate() != null) {
      whereClauseBuilder.append(" and o.createdAt >= :startDate ");
      params.put("startDate", criteria.getStartDate());
    }

    if (criteria.getEndDate() != null) {
      whereClauseBuilder.append(" and o.createdAt <= :endDate ");
      params.put("endDate", criteria.getEndDate());
    }

    return whereClauseBuilder.toString();
  }
}
