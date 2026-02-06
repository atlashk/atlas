package org.atlas.services.product.infrastructure.persistence.jpa.repository;

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
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.persistence.jpa.specification.QueryFilter;
import org.atlas.libs.persistence.jpa.specification.QueryOperator;
import org.atlas.libs.persistence.jpa.specification.QuerySpecification;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProduct;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJpaProductRepositoryUsingCriteria implements CustomJpaProductRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaProduct> findByCriteria(ProductRepository.FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaProduct> criteriaQuery = criteriaBuilder.createQuery(
        JpaProduct.class);
    Root<JpaProduct> root = criteriaQuery.from(JpaProduct.class);

    root.join("details", JoinType.LEFT);
    root.join("attributes", JoinType.LEFT);
    root.join("brand", JoinType.LEFT);
    root.join("categories", JoinType.LEFT);

    Specification<JpaProduct> spec = buildSpec(criteria);
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

    TypedQuery<JpaProduct> query = entityManager.createQuery(criteriaQuery);

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
    Root<JpaProduct> root = query.from(JpaProduct.class);

    root.join("details", JoinType.LEFT);
    root.join("attributes", JoinType.LEFT);
    root.join("brand", JoinType.LEFT);
    root.join("categories", JoinType.LEFT);

    query.select(criteriaBuilder.countDistinct(root.get("productId")));

    Specification<JpaProduct> spec = buildSpec(params);
    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
    query.where(predicate);

    return entityManager.createQuery(query).getSingleResult();
  }

  private Specification<JpaProduct> buildSpec(ProductRepository.FindProductCriteria criteria) {
    QuerySpecification<JpaProduct> spec = new QuerySpecification<>();
    if (criteria.getProductId() != null) {
      spec.addFilter(QueryFilter.of("productId", criteria.getProductId(), QueryOperator.EQUAL));
    }
    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      String lowercaseKeyword = "%" + criteria.getKeyword().toLowerCase() + "%";
      spec.addFilter(QueryFilter.or(
          QueryFilter.Condition.of("lower(name)", lowercaseKeyword, QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(detail.description)", lowercaseKeyword,
              QueryOperator.LIKE),
          QueryFilter.Condition.of("lower(attributes.value)", lowercaseKeyword,
              QueryOperator.LIKE)));
    }
    if (criteria.getMinPrice() != null) {
      spec.addFilter(
          QueryFilter.of("price", criteria.getMinPrice(), QueryOperator.GREATER_THAN_EQUAL));
    }
    if (criteria.getMaxPrice() != null) {
      spec.addFilter(
          QueryFilter.of("price", criteria.getMaxPrice(), QueryOperator.LESS_THAN_EQUAL));
    }
    if (criteria.getStockStatus() != null) {
      spec.addFilter(QueryFilter.of("stockStatus", criteria.getStockStatus(), QueryOperator.EQUAL));
    }
    if (criteria.getAvailableFrom() != null) {
      spec.addFilter(QueryFilter.of("availableFrom", criteria.getAvailableFrom(),
          QueryOperator.GREATER_THAN_EQUAL));
    }
    if (criteria.getIsActive() != null) {
      spec.addFilter(QueryFilter.of("isActive", criteria.getIsActive(), QueryOperator.EQUAL));
    }
    if (criteria.getBrandId() != null) {
      spec.addFilter(QueryFilter.of("brand.id", criteria.getBrandId(), QueryOperator.EQUAL));
    }
    if (CollectionUtil.isNotEmpty(criteria.getCategoryIds())) {
      spec.addFilter(QueryFilter.of("categories.id", criteria.getCategoryIds(), QueryOperator.IN));
    }

    return spec;
  }
}
