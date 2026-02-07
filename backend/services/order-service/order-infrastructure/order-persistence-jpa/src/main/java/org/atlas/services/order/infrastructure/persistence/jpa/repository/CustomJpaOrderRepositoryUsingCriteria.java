package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.persistence.jpa.specification.QueryFilter;
import org.atlas.libs.persistence.jpa.specification.QueryOperator;
import org.atlas.libs.persistence.jpa.specification.QuerySpecification;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrderEntity;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaOrderRepositoryUsingCriteria implements CustomJpaOrderRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaOrderEntity> findByCriteria(OrderRepository.FindOrderCriteria criteria,
      PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaOrderEntity> criteriaQuery = criteriaBuilder.createQuery(JpaOrderEntity.class);
    Root<JpaOrderEntity> root = criteriaQuery.from(JpaOrderEntity.class);

    // Fetch join for orderItems to avoid N+1 problem
    root.fetch("orderItems", JoinType.LEFT);

    Specification<JpaOrderEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

    criteriaQuery.select(root)
        .where(predicate)
        .distinct(true);

    // Sorting
    if (pagingRequest.hasSort()) {
      if (pagingRequest.isSortAscending()) {
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(pagingRequest.getSortBy())));
      } else {
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get(pagingRequest.getSortBy())));
      }
    }

    TypedQuery<JpaOrderEntity> query = entityManager.createQuery(criteriaQuery);

    // Paging
    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(OrderRepository.FindOrderCriteria criteria) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
    Root<JpaOrderEntity> root = query.from(JpaOrderEntity.class);

    query.select(criteriaBuilder.countDistinct(root.get("id")));

    Specification<JpaOrderEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaOrderEntity> buildSpec(OrderRepository.FindOrderCriteria criteria) {
    QuerySpecification<JpaOrderEntity> spec = new QuerySpecification<>();

    if (criteria.getId() != null) {
      spec.addFilter(QueryFilter.of("id", criteria.getId(), QueryOperator.EQUAL));
    }

    if (criteria.getUserId() != null) {
      spec.addFilter(QueryFilter.of("userId", criteria.getUserId(), QueryOperator.EQUAL));
    }

    if (criteria.getProductId() != null) {
      spec.addFilter(
          QueryFilter.of("orderItems.productId", criteria.getProductId(), QueryOperator.EQUAL));
    }

    if (criteria.getStatus() != null) {
      spec.addFilter(QueryFilter.of("status", criteria.getStatus(), QueryOperator.EQUAL));
    }

    if (criteria.getStartDate() != null) {
      spec.addFilter(
          QueryFilter.of("createdAt", criteria.getStartDate(), QueryOperator.GREATER_THAN_EQUAL));
    }

    if (criteria.getEndDate() != null) {
      spec.addFilter(
          QueryFilter.of("createdAt", criteria.getEndDate(), QueryOperator.LESS_THAN_EQUAL));
    }

    return spec;
  }
}
