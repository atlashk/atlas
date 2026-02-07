package org.atlas.services.iam.infrastructure.persistence.jpa.repository;

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
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaUserRepositoryUsingCriteria implements CustomJpaUserRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaUserEntity> findByCriteria(UserRepository.FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaUserEntity> criteriaQuery = criteriaBuilder.createQuery(JpaUserEntity.class);
    Root<JpaUserEntity> root = criteriaQuery.from(JpaUserEntity.class);

    Specification<JpaUserEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
    criteriaQuery.select(root).where(predicate);

    if (pagingRequest.hasSort()) {
      if (pagingRequest.isSortAscending()) {
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(pagingRequest.getSortBy())));
      } else {
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get(pagingRequest.getSortBy())));
      }
    }

    TypedQuery<JpaUserEntity> query = entityManager.createQuery(criteriaQuery);

    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(UserRepository.FindUserCriteria criteria) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
    Root<JpaUserEntity> root = query.from(JpaUserEntity.class);

    query.select(criteriaBuilder.countDistinct(root.get("id")));

    Specification<JpaUserEntity> spec = buildSpec(criteria);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaUserEntity> buildSpec(UserRepository.FindUserCriteria criteria) {
    QuerySpecification<JpaUserEntity> spec = new QuerySpecification<>();
    if (criteria == null) {
      return spec;
    }

    if (StringUtil.isNotBlank(criteria.getId())) {
      spec.addFilter(QueryFilter.of("id", criteria.getId().trim(), QueryOperator.EQUAL));
    }

    if (StringUtil.isNotBlank(criteria.getUsername())) {
      spec.addFilter(
          QueryFilter.of("username", criteria.getUsername().trim(), QueryOperator.LIKE));
    }

    if (StringUtil.isNotBlank(criteria.getFirstName())) {
      spec.addFilter(
          QueryFilter.of("firstName", criteria.getFirstName().trim(), QueryOperator.LIKE));
    }

    if (StringUtil.isNotBlank(criteria.getLastName())) {
      spec.addFilter(
          QueryFilter.of("lastName", criteria.getLastName().trim(), QueryOperator.LIKE));
    }

    if (StringUtil.isNotBlank(criteria.getEmail())) {
      spec.addFilter(QueryFilter.of("email", criteria.getEmail().trim(), QueryOperator.LIKE));
    }

    if (StringUtil.isNotBlank(criteria.getPhoneNumber())) {
      spec.addFilter(
          QueryFilter.of("phoneNumber", criteria.getPhoneNumber().trim(), QueryOperator.LIKE));
    }

    if (criteria.getRole() != null) {
      spec.addFilter(QueryFilter.of("role", criteria.getRole(), QueryOperator.EQUAL));
    }

    return spec;
  }
}
