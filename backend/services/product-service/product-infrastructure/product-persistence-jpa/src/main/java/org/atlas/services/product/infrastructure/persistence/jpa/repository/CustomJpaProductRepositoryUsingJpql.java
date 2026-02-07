package org.atlas.services.product.infrastructure.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class CustomJpaProductRepositoryUsingJpql implements CustomJpaProductRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaProductEntity> findByCriteria(ProductRepository.FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    StringBuilder sqlBuilder = new StringBuilder("""
        select p
        from JpaProduct p
        left join fetch p.details d
        left join fetch p.attributes a
        left join fetch p.brand b
        left join fetch p.categories c
        """);

    Map<String, Object> params = new HashMap<>();
    sqlBuilder.append(buildWhereClause(criteria, params));

    // Sorting
    if (pagingRequest.hasSort()) {
      sqlBuilder.append(" order by p.").append(pagingRequest.getSortBy());
      if (pagingRequest.isSortDescending()) {
        sqlBuilder.append(" desc");
      }
    }

    String sql = sqlBuilder.toString();
    TypedQuery<JpaProductEntity> query = entityManager.createQuery(sql, JpaProductEntity.class);

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
  public long countByCriteria(ProductRepository.FindProductCriteria criteria) {
    Map<String, Object> params = new HashMap<>();
    String whereClause = buildWhereClause(criteria, params);
    String countSql = """
        select count(distinct p.productId)
        from JpaProductEntity p
        left join p.details d
        left join p.attributes a
        left join p.brand b
        left join p.categories c
        """ + whereClause;
    TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
    params.forEach(countQuery::setParameter);
    return countQuery.getSingleResult();
  }

  private String buildWhereClause(ProductRepository.FindProductCriteria criteria, Map<String, Object> params) {
    StringBuilder whereClauseBuilder = new StringBuilder("where 1=1 ");
    if (criteria.getProductId() != null) {
      whereClauseBuilder.append(" and p.productId = :productId ");
      params.put("productId", criteria.getProductId());
    }
    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      whereClauseBuilder.append("""
          and (
            lower(p.name) like :keyword
            or lower(d.description) like :keyword
            or lower(a.value) like :keyword
          )
          """);
      params.put("keyword", "%" + criteria.getKeyword().toLowerCase() + "%");
    }
    if (criteria.getMinPrice() != null) {
      whereClauseBuilder.append(" and p.price >= :minPrice ");
      params.put("minPrice", criteria.getMinPrice());
    }
    if (criteria.getMaxPrice() != null) {
      whereClauseBuilder.append(" and p.price <= :maxPrice ");
      params.put("maxPrice", criteria.getMaxPrice());
    }
    if (criteria.getStockStatus() != null) {
      whereClauseBuilder.append(" and p.stockStatus = :stockStatus ");
      params.put("stockStatus", criteria.getStockStatus());
    }
    if (criteria.getAvailableFrom() != null) {
      whereClauseBuilder.append(" and p.availableFrom >= :availableFrom ");
      params.put("availableFrom", criteria.getAvailableFrom());
    }
    if (criteria.getIsActive() != null) {
      whereClauseBuilder.append(" and p.isActive = :isActive ");
      params.put("isActive", criteria.getIsActive());
    }
    if (criteria.getBrandId() != null) {
      whereClauseBuilder.append(" and b.id = :brandId ");
      params.put("brandId", criteria.getBrandId());
    }
    if (CollectionUtil.isNotEmpty(criteria.getCategoryIds())) {
      whereClauseBuilder.append(" and c.id IN (:categoryIds) ");
      params.put("categoryIds", criteria.getCategoryIds());
    }
    return whereClauseBuilder.toString();
  }
}
