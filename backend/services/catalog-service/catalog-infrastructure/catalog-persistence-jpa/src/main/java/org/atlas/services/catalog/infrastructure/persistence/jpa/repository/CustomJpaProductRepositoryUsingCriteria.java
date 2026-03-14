package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.persistence.jpa.specification.QueryFilter;
import org.atlas.libs.persistence.jpa.specification.QueryOperator;
import org.atlas.libs.persistence.jpa.specification.QuerySpecification;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaProductRepositoryUsingCriteria implements CustomJpaProductRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaProductEntity> findByCriteria(ProductRepository.FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaProductEntity> criteriaQuery = criteriaBuilder.createQuery(
        JpaProductEntity.class);
    Root<JpaProductEntity> root = criteriaQuery.from(JpaProductEntity.class);

    root.fetch("details", JoinType.LEFT);
    root.fetch("attributes", JoinType.LEFT);
    root.fetch("brand", JoinType.LEFT);
    root.fetch("categories", JoinType.LEFT);

    Specification<JpaProductEntity> spec = buildSpec(criteria);
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

    TypedQuery<JpaProductEntity> query = entityManager.createQuery(criteriaQuery);

    // Paging
    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(ProductRepository.FindProductCriteria params) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
    Root<JpaProductEntity> root = query.from(JpaProductEntity.class);

    root.join("details", JoinType.LEFT);
    root.join("attributes", JoinType.LEFT);
    root.join("brand", JoinType.LEFT);
    root.join("categories", JoinType.LEFT);

    query.select(criteriaBuilder.countDistinct(root.get("id")));

    Specification<JpaProductEntity> spec = buildSpec(params);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaProductEntity> buildSpec(
      ProductRepository.FindProductCriteria criteria) {
    QuerySpecification<JpaProductEntity> spec = new QuerySpecification<>();

    // ID
    if (criteria.getId() != null) {
      spec.addFilter(QueryFilter.of("id", criteria.getId(), QueryOperator.EQUAL));
    }

    // Keyword
    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      String lowercaseKeyword = "%" + criteria.getKeyword().toLowerCase() + "%";
      spec.addFilter(QueryFilter.or(
          QueryFilter.Condition.of("lower(name)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(detail.description)", lowercaseKeyword,
              QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(attributes.value)", lowercaseKeyword,
              QueryOperator.LIKE)));
    }

    // Type
    if (criteria.getType() != null) {
      spec.addFilter(QueryFilter.of("type", criteria.getType(), QueryOperator.EQUAL));
    }

    // Price
    if (criteria.getMinPrice() != null) {
      spec.addFilter(
          QueryFilter.of("price", criteria.getMinPrice(), QueryOperator.GREATER_THAN_EQUAL));
    }
    if (criteria.getMaxPrice() != null) {
      spec.addFilter(
          QueryFilter.of("price", criteria.getMaxPrice(), QueryOperator.LESS_THAN_EQUAL));
    }

    // Published date
    if (criteria.getStartPublishedDate() != null) {
      LocalDateTime midnight = DateUtil.getMidnight(criteria.getStartPublishedDate());
      spec.addFilter(QueryFilter.of("publishedAt", midnight, QueryOperator.GREATER_THAN_EQUAL));
    }
    if (criteria.getEndPublishedDate() != null) {
      LocalDateTime nextMidnight = DateUtil.getNextMidnight(criteria.getEndPublishedDate());
      spec.addFilter(QueryFilter.of("publishedAt", nextMidnight, QueryOperator.LESS_THAN));
    }

    // In stock
    if (criteria.getInStock() != null) {
      spec.addFilter(QueryFilter.of("inStock", criteria.getInStock(), QueryOperator.EQUAL));
    } else {
      // Default to only show in-stock products
      spec.addFilter(QueryFilter.of("inStock", true, QueryOperator.EQUAL));
    }

    // Brand
    if (criteria.getBrandId() != null) {
      spec.addFilter(QueryFilter.of("brand.id", criteria.getBrandId(), QueryOperator.EQUAL));
    }

    // Categories
    if (CollectionUtil.isNotEmpty(criteria.getCategoryIds())) {
      spec.addFilter(QueryFilter.of("categories.id", criteria.getCategoryIds(), QueryOperator.IN));
    }

    return spec;
  }
}
