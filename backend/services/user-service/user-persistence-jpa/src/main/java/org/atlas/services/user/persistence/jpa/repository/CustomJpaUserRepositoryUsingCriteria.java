package org.atlas.services.user.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.persistence.jpa.specification.QueryFilter;
import org.atlas.libs.persistence.jpa.specification.QueryOperator;
import org.atlas.libs.persistence.jpa.specification.QuerySpecification;
import org.atlas.services.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.services.user.persistence.jpa.entity.JpaUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaUserRepositoryUsingCriteria implements CustomJpaUserRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaUser> findByCriteria(FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaUser> criteriaQuery = criteriaBuilder.createQuery(JpaUser.class);
    Root<JpaUser> root = criteriaQuery.from(JpaUser.class);

    Specification<JpaUser> spec = buildSpec(criteria);
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

    TypedQuery<JpaUser> query = entityManager.createQuery(criteriaQuery);

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
    Root<JpaUser> root = query.from(JpaUser.class);

    query.select(criteriaBuilder.count(root.get("id")));

    Specification<JpaUser> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaUser> buildSpec(FindUserCriteria criteria) {
    QuerySpecification<JpaUser> spec = new QuerySpecification<>();

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
