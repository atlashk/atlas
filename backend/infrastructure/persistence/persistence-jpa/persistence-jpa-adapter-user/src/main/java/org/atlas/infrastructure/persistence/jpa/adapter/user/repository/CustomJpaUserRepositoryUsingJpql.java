package org.atlas.infrastructure.persistence.jpa.adapter.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUserEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class CustomJpaUserRepositoryUsingJpql implements CustomJpaUserRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaUserEntity> findByCriteria(FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    StringBuilder sqlBuilder = new StringBuilder("""
        select u
        from JpaUserEntity u
        """);

    Map<String, Object> params = new HashMap<>();
    sqlBuilder.append(buildWhereClause(criteria, params));

    // Sorting
    if (pagingRequest.hasSort()) {
      sqlBuilder.append(" order by u.").append(pagingRequest.getSortBy());
      if (pagingRequest.isSortDescending()) {
        sqlBuilder.append(" desc");
      }
    }

    String sql = sqlBuilder.toString();
    TypedQuery<JpaUserEntity> query = entityManager.createQuery(sql, JpaUserEntity.class);

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
  public long countByCriteria(FindUserCriteria criteria) {
    Map<String, Object> params = new HashMap<>();
    String whereClause = buildWhereClause(criteria, params);
    String countSql = """
        select count(u.id)
        from JpaUserEntity u
        """ + whereClause;
    TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
    params.forEach(countQuery::setParameter);
    return countQuery.getSingleResult();
  }

  private String buildWhereClause(FindUserCriteria criteria, Map<String, Object> params) {
    StringBuilder whereClauseBuilder = new StringBuilder("where 1=1 ");

    if (criteria.getId() != null) {
      whereClauseBuilder.append(" and u.id = :id ");
      params.put("id", criteria.getId());
    }

    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      whereClauseBuilder.append("""
          and (
            lower(u.username) like :keyword
            or lower(u.firstName) like :keyword
            or lower(u.lastName) like :keyword
            or lower(u.email) like :keyword
            or lower(u.phoneNumber) like :keyword
          )
          """);
      params.put("keyword", "%" + criteria.getKeyword().toLowerCase() + "%");
    }
    
    if (criteria.getRole() != null) {
      whereClauseBuilder.append(" and u.role = :role ");
      params.put("role", criteria.getRole());
    }
    
    return whereClauseBuilder.toString();
  }
}