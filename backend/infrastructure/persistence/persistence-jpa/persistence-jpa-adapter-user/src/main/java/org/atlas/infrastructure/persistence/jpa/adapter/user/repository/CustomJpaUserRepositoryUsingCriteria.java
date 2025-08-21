package org.atlas.infrastructure.persistence.jpa.adapter.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.core.specification.QueryFilter;
import org.atlas.infrastructure.persistence.jpa.core.specification.QueryOperator;
import org.atlas.infrastructure.persistence.jpa.core.specification.QuerySpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaUserRepositoryUsingCriteria implements CustomJpaUserRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaUserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaUserEntity> criteriaQuery = criteriaBuilder.createQuery(JpaUserEntity.class);
    Root<JpaUserEntity> root = criteriaQuery.from(JpaUserEntity.class);

    Specification<JpaUserEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
    criteriaQuery.where(predicate);

    // Sorting
    if (pagingRequest.hasSort()) {
      if (pagingRequest.isSortAscending()) {
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(pagingRequest.getSortBy())));
      } else {
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get(pagingRequest.getSortBy())));
      }
    }

    TypedQuery<JpaUserEntity> query = entityManager.createQuery(criteriaQuery);

    // Paging
    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(FindUserCriteria criteria) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
    Root<JpaUserEntity> root = query.from(JpaUserEntity.class);

    query.select(criteriaBuilder.count(root.get("id")));

    Specification<JpaUserEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaUserEntity> buildSpec(FindUserCriteria criteria) {
    QuerySpecification<JpaUserEntity> spec = new QuerySpecification<>();
    
    if (criteria.getId() != null) {
      spec.addFilter(QueryFilter.of("id", criteria.getId(), QueryOperator.EQUAL));
    }
    
    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      String lowercaseKeyword = "%" + criteria.getKeyword().toLowerCase() + "%";
      spec.addFilter(QueryFilter.or(
          QueryFilter.Condition.of("lower(username)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(firstName)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(lastName)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(email)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(phoneNumber)", lowercaseKeyword, QueryOperator.LIKE)));
    }

    if (criteria.getRole() != null) {
      spec.addFilter(QueryFilter.of("role", criteria.getRole(), QueryOperator.EQUAL));
    }

    return spec;
  }
}
